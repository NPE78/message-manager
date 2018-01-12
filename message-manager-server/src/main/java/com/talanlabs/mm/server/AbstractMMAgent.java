package com.talanlabs.mm.server;

import com.talanlabs.mm.engine.IMMProcess;
import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.MMEngine;
import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.engine.model.ProcessingResultBuilder;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.mm.server.model.ProcessContext;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An agent is the main class of a process. It is used by the Process Manager and {@link com.talanlabs.processmanager.engine.ProcessManager#handle(String, String, Serializable)} method
 */
public abstract class AbstractMMAgent<F extends AbstractMMFlux> extends AbstractAgent implements IMMProcess<F> {

    private final LogService logService;

    private final Class<F> fluxClass;

    private final String messageTypeName;

    /**
     * Since the agent is used by many threads at the same time, we need to use a thread local to ensure the process context is thread-safe
     */
    private final ThreadLocal<F> fluxThreadLocal;

    /**
     * Create an agent with given name
     *
     * @param fluxClass the managed flux class from which the agent name is deducted
     */
    public AbstractMMAgent(Class<F> fluxClass) {
        super(fluxClass.getSimpleName());

        logService = com.talanlabs.processmanager.shared.logging.LogManager.getLogService(getClass());

        this.fluxClass = fluxClass;
        this.messageTypeName = fluxClass.getSimpleName();
        this.fluxThreadLocal = new ThreadLocal<>();
    }

    public final LogService getLogService() {
        return logService;
    }

    /**
     * Register the agent to the system
     */
    public void identificate() {
    }

    @Override
    public final void work(Serializable messageObject, String engineUuid) {
        try {
            dispatchMessage(messageObject, engineUuid);
        } catch (Exception e) {
            getLogService().error(() -> messageTypeName, e);
            Map<IProcessError, ErrorImpact> errorMap = new HashMap<>();
            errorMap.put(addError("UNKNOWN_ERROR"), new ErrorImpact(ErrorRecyclingKind.MANUAL, null, null));
            reject(errorMap);
            notifyMessageStatus(MessageStatus.TO_RECYCLE_MANUALLY);
        }
    }

    /**
     * If the message concerns this agent, uses the mm engine to track any progress during the process
     */
    @SuppressWarnings("unchecked")
    public final IProcessingResult dispatchMessage(Serializable messageObject, String engineUuid) {
        if (isConcerned(messageObject)) {
            F message = (F) messageObject;

            message.getProcessContext().init(engineUuid);

            fluxThreadLocal.set(message);
            return getMMEngine(engineUuid).start(message, this, getMMDictionary());
        }
        return ProcessingResultBuilder.rejectDefinitely(null, null); // if the message does not concern this agent, we reject it
    }

    private MMEngine getMMEngine(String engineUuid) {
        return MMEngineAddon.getEngine(engineUuid);
    }

    /**
     * The message type name configured for this agent
     */
    @Override
    public final String getMessageTypeName() {
        return messageTypeName;
    }

    /**
     * Is the agent concerned by the message it received?
     *
     * @param messageObject The message object received from an Handle
     * @return true if the process method must be called
     */
    private boolean isConcerned(Serializable messageObject) {
        return messageObject != null && fluxClass.isAssignableFrom(messageObject.getClass());
    }

    /**
     * Get the current message. Uses the thread local because of the multi-threading configuration
     */
    protected final F getMessage() {
        return fluxThreadLocal.get();
    }

    private ProcessContext getProcessContext() {
        return getMessage().getProcessContext();
    }

    private IProcessErrorFactory getProcessErrorFactory() {
        return getProcessContext().getProcessErrorFactory();
    }

    private MMDictionary getMMDictionary() {
        return MMEngineAddon.getDictionary(getProcessContext().getEngineUuid());
    }

    /**
     * Add an error to the process
     */
    protected final IProcessError addError(String errorCode) {
        IProcessError processError = getProcessErrorFactory().createProcessError(errorCode);
        getProcessContext().addProcessError(processError);
        return processError;
    }

    @Override
    public List<IProcessError> getProcessErrorList() {
        return getProcessContext().getProcessErrorList();
    }
}
