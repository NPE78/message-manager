package com.talanlabs.mm.server;

import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.exception.ContentNotFetchedException;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.exceptions.AgentException;
import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * An abstract agent to inject and monitor import files in a folder, using the message manager
 *
 * @param <F> the type of flux managed by this agent
 */
public abstract class AbstractMMImportAgent<F extends AbstractMMImportFlux> extends AbstractMMAgent<F> {

    private final UnderlyingAgent underlyingAgent;

    /**
     * Create an agent with given name
     *
     * @param fluxClass the managed flux class from which the agent name is deducted
     */
    public AbstractMMImportAgent(Class<F> fluxClass) {
        super(fluxClass);

        underlyingAgent = new UnderlyingAgent();
    }

    @Override
    public MessageWay getMessageWay() {
        return MessageWay.IN;
    }

    @Override
    public void register(String engineUuid, int maxWorking) {
        throw new AgentException("You have to register this agent using register(engineUuid, maxWorking, delay, basePath) method");
    }

    public void register(String engineUuid, int maxWorking, long delay, File basePath) {
        MMEngineAddon.registerMessageType(engineUuid, buildMessageType());
        underlyingAgent.register(engineUuid, maxWorking, delay, basePath);

        super.register(engineUuid);
    }

    /**
     * Returns the working folder of the injector/agent. This is the folder which is monitored for new messages
     */
    public final File getWorkDir() {
        return underlyingAgent.getWorkDir();
    }

    @Override
    public final void accept(Map<IProcessError, ErrorImpact> errorMap) {
        // we don't move the file anymore, its done by the injector
        saveErrors(errorMap);
        F message = getMessage();
        if (message != null) {
            underlyingAgent.acceptFile(message.getFile());
        }
        acceptMessage();
    }

    @Override
    public final void reject(Map<IProcessError, ErrorImpact> errorMap) {
        // we don't move the file anymore, its done by the injector
        saveErrors(errorMap);
        F message = getMessage();
        if (message != null) {
            underlyingAgent.rejectFile(message.getFile());
        }
        rejectMessage();
    }

    protected abstract F createFlux();

    private void injectMessage(F flux) {
        String engineUuid = flux.getProcessContext().getEngineUuid();

        manageDeadlineDate(flux);

        saveOrUpdateMessage(flux);

        File file = flux.getFile();
        if (file != null && !flux.getId().toString().equals(file.getName())) {
            renameAndMoveFile(flux, file);
        }

        if (flux.getMessageStatus() != MessageStatus.REJECTED) {
            getLogService().debug(() -> "[{0}] Inject: {1}", flux.getMessageType() != null ? flux.getMessageType().getName() : "", flux.getId());
            ProcessManager.handle(engineUuid, getName(), flux);
        }
    }

    private void manageDeadlineDate(F flux) {
        if (flux.getFirstProcessingDate() == null) {
            flux.setFirstProcessingDate(Instant.now());
        }
        if (flux.getMessageType() != null && flux.getMessageType().getRecyclingDeadline() != null) {
            Instant deadlineDate = flux.getFirstProcessingDate().plus(flux.getMessageType().getRecyclingDeadline(), ChronoUnit.MINUTES);
            flux.setDeadlineDate(deadlineDate);
        }
        // check deadline_date, only if it's not a new message
        if (flux.getFile() == null && flux.getDeadlineDate() != null && flux.getDeadlineDate().isBefore(Instant.now())) {
            flux.setMessageStatus(MessageStatus.REJECTED);
            getLogService().warn(() -> "Message {0} has reached its deadline, it has been rejected", flux.getId());
        }
    }

    /**
     * We want the file to be renamed to the id of the message so that if the file ever had to be rejected, it would consider the previous state of the message<br>
     * Also, we move the file to the accepted directory<br>
     * This happens prior to the #handle method call
     */
    private void renameAndMoveFile(F flux, File file) {
        // this can not happen if the message is being rejected: it would have no file
        File dest = new File(file.getParentFile(), "accepted" + File.separator + flux.getId().toString());
        boolean moved = file.renameTo(dest); //$NON-NLS-1$
        if (moved) {
            flux.setFile(dest);
            flux.setFolder("accepted");
            saveOrUpdateMessage(flux);
        }
    }

    @Override
    protected void prepare(F message) {
        try {
            message.setContent(getFluxContentManager().getContent(message));
        } catch (ContentNotFetchedException e) {
            getLogService().warn(() -> "The content could not be fetched for message {0}", e, message.getId());
            reject(getSingleUnknownErrorMap(message.getProcessContext().getEngineUuid(), ErrorRecyclingKind.MANUAL));
        }
    }

    /**
     * The message has been accepted
     */
    protected abstract void acceptMessage();

    /**
     * The message has been rejected
     */
    protected abstract void rejectMessage();

    private void saveErrors(Map<IProcessError, ErrorImpact> errorMap) {
        if (errorMap != null) {
            errorMap.forEach(this::saveMessageError);
        }
    }

    @SuppressWarnings("unchecked")
    F getMessage(UnderlyingImportFlux underlyingImportFlux) {
        return (F) underlyingImportFlux.message;
    }

    protected abstract void saveMessageError(IProcessError processError, ErrorImpact errorImpact);

    /**
     * Agent which manages files coming from the file system and redirects them to the parent agent
     */
    private class UnderlyingAgent extends AbstractFileAgent<UnderlyingImportFlux> {

        UnderlyingAgent() {
            super(UnderlyingImportFlux.class, AbstractMMImportAgent.this.getName());
        }

        @Override
        public void doWork(UnderlyingImportFlux underlyingFlux, String engineUuid) {
            // we shouldn't get there, but if we do, this is what we would do
            F flux = getMessage(underlyingFlux);
            AbstractMMImportAgent.this.work(flux, engineUuid);
        }

        @Override
        protected void handleFlux(UnderlyingImportFlux underlyingFlux, String engineUuid) {
            F flux = getMessage(underlyingFlux);
            flux.getProcessContext().init(engineUuid);
            flux.setMessageStatus(MessageStatus.TO_BE_INTEGRATED);
            flux.setFile(underlyingFlux.getFile());
            flux.setMessageType(MMEngineAddon.getMessageType(engineUuid, getName()));

            AbstractMMImportAgent.this.injectMessage(flux);
        }

        @Override
        protected Agent getAgent() {
            return AbstractMMImportAgent.this;
        }

        @Override
        public String getName() {
            return AbstractMMImportAgent.this.getName();
        }

        @Override
        protected UnderlyingImportFlux createFlux() {
            return new UnderlyingImportFlux(AbstractMMImportAgent.this.createFlux());
        }
    }

    private static class UnderlyingImportFlux extends AbstractImportFlux {

        private Serializable message;

        private UnderlyingImportFlux(Serializable message) {
            this.message = message;
        }
    }
}
