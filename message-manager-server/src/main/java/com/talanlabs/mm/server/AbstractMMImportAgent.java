package com.talanlabs.mm.server;

import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.agent.AbstractFileAgent;
import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
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

    private UnderlyingAgent underlyingAgent;

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
        underlyingAgent.register(engineUuid, maxWorking, delay, basePath);

        super.register(engineUuid);
    }

    @Override
    public final void accept(Map<IProcessError, ErrorImpact> errorMap) {
        // we don't move the file anymore, its done by the injector
        saveErrors(errorMap);
        acceptMessage();
    }

    @Override
    public final void reject(Map<IProcessError, ErrorImpact> errorMap) {
        // we don't move the file anymore, its done by the injector
        saveErrors(errorMap);
        rejectMessage();
    }

    protected abstract F createFlux();

    public final void injectMessage(F flux) {
        String engineUuid = flux.getProcessContext().getEngineUuid();
        flux.setMessageType(MMEngineAddon.getMessageType(engineUuid, getName()));

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

    private void renameAndMoveFile(F flux, File file) {
        // this can not happen if the message is being rejected: it would have no file
        File dest = new File(file.getParentFile(), "accepted" + File.separator + flux.getId().toString());
        boolean moved = file.renameTo(dest); //$NON-NLS-1$
        if (moved) {
            flux.setFile(dest);
            saveOrUpdateMessage(flux);
        }
    }

    protected abstract void saveOrUpdateMessage(F message);

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

    protected abstract void saveMessageError(IProcessError processError, ErrorImpact errorImpact);

    /**
     * Agent which manages files coming from the file system and redirects them to the parent agent
     */
    private class UnderlyingAgent extends AbstractFileAgent<UnderlyingImportFlux> {

        UnderlyingAgent() {
            super(UnderlyingImportFlux.class);
        }

        @Override
        public void doWork(UnderlyingImportFlux underlyingFlux, String engineUuid) {
            F flux = getMessage(underlyingFlux);
            flux.setFile(underlyingFlux.getFile());

            AbstractMMImportAgent.this.work(flux, engineUuid);
        }

        @Override
        protected void handleFlux(UnderlyingImportFlux underlyingFlux, String engineUuid) {
            F flux = getMessage(underlyingFlux);
            flux.getProcessContext().init(engineUuid);
            flux.setMessageStatus(MessageStatus.TO_BE_INTEGRATED);

            AbstractMMImportAgent.this.injectMessage(flux);
        }

        @Override
        public String getName() {
            return AbstractMMImportAgent.this.getName();
        }

        @SuppressWarnings("unchecked")
        F getMessage(UnderlyingImportFlux underlyingImportFlux) {
            return (F) underlyingImportFlux.message;
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
