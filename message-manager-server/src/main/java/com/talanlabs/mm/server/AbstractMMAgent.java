package com.talanlabs.mm.server;

import com.google.common.annotations.VisibleForTesting;
import com.talanlabs.mm.engine.IMMProcess;
import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.MMEngine;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.engine.exception.InvalidDictionaryOperationException;
import com.talanlabs.mm.engine.exception.UnknownDictionaryException;
import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.engine.model.ProcessingResultBuilder;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.delegate.FluxContentManager;
import com.talanlabs.mm.server.exception.DictionaryException;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.mm.server.model.IErrorEnum;
import com.talanlabs.mm.server.model.ProcessContext;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An agent is the main class of a process. It is used by the Process Manager and {@link com.talanlabs.processmanager.engine.PM#handle(String, String, Serializable)} method
 */
public abstract class AbstractMMAgent<F extends AbstractMMFlux> extends AbstractAgent implements IMMProcess<F> {

    private final LogService logService;

    private final Class<F> fluxClass;

    private final String messageTypeName;

    /**
     * A map which binds, for each type of agent, an engine uuid to the subdictionary of the agent (rootDictionary.messageTypeName)
     */
    private final Map<String, SubDictionary> engineDictionary;

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

        logService = LogManager.getLogService(getClass());

        this.fluxClass = fluxClass;
        this.messageTypeName = fluxClass.getSimpleName();
        this.fluxThreadLocal = new ThreadLocal<>();
        this.engineDictionary = new HashMap<>(1); // most of the time, there would be only one engine
    }

    final LogService getLogService() {
        return logService;
    }

    @Override
    public void register(String engineUuid, int maxWorking) {
        MMEngineAddon.registerMessageType(engineUuid, buildMessageType());

        super.register(engineUuid, maxWorking);
    }

    /**
     * Builds the message type
     */
    protected IMessageType buildMessageType() {
        return IMessageType.of(getName(), getMessageWay());
    }

    @Override
    public final void work(Serializable messageObject) {
        try {
            dispatchMessage(messageObject, getEngineUuid());
        } catch (Exception e) {
            getLogService().error(() -> messageTypeName, e);
            Map<IProcessError, ErrorImpact> errorMap = getSingleUnknownErrorMap(getEngineUuid(), ErrorRecyclingKind.MANUAL);
            reject(errorMap);
            notifyMessageStatus(MessageStatus.TO_RECYCLE_MANUALLY);
        }
    }

    /**
     * Called prior to the #handle method
     */
    final void handleMessage(F flux) {
        manageDeadlineDate(flux);
        saveOrUpdateMessage(flux);
    }

    @VisibleForTesting
    void manageDeadlineDate(F flux) {
        if (flux.getFirstProcessingDate() == null) {
            flux.setFirstProcessingDate(Instant.now());
        }
        if (flux.getMessageType() != null && flux.getMessageType().getRecyclingDeadline() != null) {
            Instant deadlineDate = flux.getFirstProcessingDate().plus(flux.getMessageType().getRecyclingDeadline(), ChronoUnit.MINUTES);
            flux.setDeadlineDate(deadlineDate);
        }
        // check deadline_date
        if (flux.getDeadlineDate() != null && flux.getDeadlineDate().isBefore(Instant.now())) {
            flux.setMessageStatus(MessageStatus.REJECTED);
            getLogService().warn(() -> "Message {0} has reached its deadline, it has been rejected", flux.getId());
        }
    }

    @Override
    public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
        F message = getMessage();
        message.setMessageStatus(newMessageStatus);
        message.setNextProcessingDate(nextProcessingDate);

        saveOrUpdateMessage(message);
    }

    protected abstract void saveOrUpdateMessage(F message);

    /**
     * If the message concerns this agent, uses the mm engine to track any progress during the process
     */
    @SuppressWarnings("unchecked")
    public final IProcessingResult dispatchMessage(Serializable messageObject, String engineUuid) {
        Exception exception = null;
        try {
            if (isConcerned(messageObject)) {
                F message = (F) messageObject;

                checkDictionary(engineUuid);

                message.init(engineUuid); // this message has another thread local than the one passed to the #handle method
                setCurrentMessage(message);

                prepare(message);

                return getMMEngine(engineUuid).start(message, this, getMMDictionary());
            }
        } catch (Exception e) {
            logService.error(() -> "Error occurred when dispatching message to engine " + engineUuid, e);
            exception = e;
        }
        IProcessingResult processingResult = ProcessingResultBuilder.rejectDefinitely(getSingleUnknownErrorMap(engineUuid, ErrorRecyclingKind.NOT_RECYCLABLE), exception);
        getMMEngine(engineUuid).reject(this, processingResult);
        return processingResult; // if the message does not concern this agent, we reject it
    }

    /**
     * Initializes the dictionary of the agent (message type) and adds all known errors (no database involved)
     */
    private void checkDictionary(String engineUuid) {
        try {
            synchronized (engineDictionary) {
                engineDictionary.computeIfAbsent(engineUuid, this::getOrCreateSubsetDictionary);
            }
        } catch (DictionaryException e) {
            logService.error(() -> "Exception when creating dictionary " + getMessageTypeName(), e);
        }
    }

    private SubDictionary getOrCreateSubsetDictionary(String engineUuid) {
        try {
            SubDictionary subsetDictionary = MMEngineAddon.getDictionary(engineUuid).getOrCreateSubsetDictionary(getMessageTypeName());
            enrichDictionary(subsetDictionary, engineUuid);
            return subsetDictionary;
        } catch (InvalidDictionaryOperationException e) {
            throw new DictionaryException(e);
        }
    }

    /**
     * If some errors have to be added to the message dictionary, do it here<br>
     * The dictionary is the one of the agent (message type : MAIN.messageTypeName)<br>
     * Use {@link #registerErrorEnum(Class, SubDictionary)} to add a whole lot of errors quickly
     */
    protected void enrichDictionary(SubDictionary subDictionary, String engineUuid) {
    }

    /**
     * Registers a whole enumeration of errors to the given dictionary
     */
    protected final void registerErrorEnum(Class<? extends IErrorEnum> errorEnumClass, SubDictionary dictionary) {
        if (errorEnumClass.isEnum()) {
            IErrorEnum[] enumConstants = errorEnumClass.getEnumConstants();
            for (IErrorEnum error : enumConstants) {
                dictionary.defineError(error);
            }
        }
    }

    /**
     * Override this method to do extra stuff before the message being processed by the message manager engine<br>
     * For instance, if this message is being recycling, you might want to define previous IGNORED errors as WARNING in the dictionary
     */
    protected void prepare(F message) {
        // nothing for this implementation
    }

    /**
     * Sets the current message in the thread local
     */
    final void setCurrentMessage(F message) {
        fluxThreadLocal.set(message);
    }

    protected final Map<IProcessError, ErrorImpact> getSingleUnknownErrorMap(String engineUuid, ErrorRecyclingKind errorRecyclingKind) {
        Map<IProcessError, ErrorImpact> errorMap = new HashMap<>();
        IProcessError unknownError = MMEngineAddon.getProcessErrorFactory(engineUuid).createProcessError("UNKNOWN_ERROR");
        errorMap.put(unknownError, ErrorImpact.of(errorRecyclingKind));
        return errorMap;
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
        return getMessage().getDictionary();
    }

    protected final FluxContentManager getFluxContentManager() {
        return getMessage().getFluxContentManager();
    }

    /**
     * Add an error to the process
     */
    protected final IProcessError addError(IErrorEnum error) {
        return addError(error.getCode());
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
    public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
        SubDictionary dictionary = rootDictionary;
        try {
            dictionary = rootDictionary.getSubsetDictionary(getMessageTypeName());
            F message = getMessage();
            if (message != null && message.getId() != null) {
                dictionary = dictionary.getOrCreateSubsetDictionary(message.getId().toString());
                dictionary.setBurnAfterUse(true);
            }
        } catch (UnknownDictionaryException | InvalidDictionaryOperationException e) {
            logService.info(() -> "Exception when creating dictionary " + getMessageTypeName(), e);
        }
        return dictionary;
    }

    @Override
    public List<IProcessError> getProcessErrorList() {
        return getProcessContext().getProcessErrorList();
    }
}
