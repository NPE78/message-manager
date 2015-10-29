package com.synaptix.mm.engine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.IProcessContext;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;
import com.synaptix.pmgr.core.apis.Engine;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;

/**
 * Always use Guice to create an agent properly
 * An agent is the main class of a process. It is used by the Process Manager and {@link ProcessEngine#handle(String, Object)} method
 * Created by NicolasP on 23/10/2015.
 */
public abstract class AbstractMMAgent<C extends IProcessContext> implements ProcessingChannel.Agent {

	private static final Log LOG = LogFactory.getLog(AbstractMMAgent.class);

	private final String messageTypeName;

	/**
	 * Since the agent is used by many threads at the same time, we need to use a thread local to ensure the process context is thread-safe
	 */
	private final ThreadLocal<C> processContextThreadLocal;

	@Inject
	private IProcessErrorFactory processErrorFactory;

	@Inject
	private MMDictionary dictionary;

	/**
	 * Create an agent with given name
	 *
	 * @param messageTypeName the name of the message type
	 */
	@Inject
	public AbstractMMAgent(String messageTypeName) {
		super();

		this.messageTypeName = messageTypeName;
		this.processContextThreadLocal = new ThreadLocal<>();
	}

	@Override
	public final void work(Object messageObject, Engine engine) {
		try {
			doWork(messageObject);
		} catch (Exception e) {
			LOG.error(messageTypeName, e);
			addError("UNKNOWN_ERROR", null, e.getLocalizedMessage());
			notifyMessageStatus(MessageStatus.TO_RECYCLE_MANUALLY);
			reject();
		}
	}

	public final IProcessingResult doWork(Object messageObject) {
		IProcessingResult processingResult = null;
		if (isConcerned(messageObject)) {
			C processContext = buildProcessContext(messageObject);
			processContextThreadLocal.set(processContext);

			processContext.setProcessErrorList(new ArrayList<>());

			notifyMessageStatus(MessageStatus.IN_PROGRESS);

			process(messageObject);

			processingResult = dictionary.getProcessingResult(messageTypeName, processContextThreadLocal.get().getProcessErrorList());

			if (checkBlocking(processingResult)) {
				reject();
			} else {
				accept();
			}
		} else {
			// we could consider doing something here...
		}
		return processingResult;
	}

	/**
	 * Called to notify the implementing class that the message status has to be changed.
	 * If needed, use this to update the message in the database or elsewhere.
	 */
	protected abstract void notifyMessageStatus(MessageStatus newMessageStatus);

	/**
	 * The message type name configured for this agent
	 */
	protected final String getMessageTypeName() {
		return messageTypeName;
	}

	/**
	 * Is the agent concerned by the message it received?
	 *
	 * @param messageObject The message object received from an Handle
	 * @return true if the process method must be called
	 */
	protected abstract boolean isConcerned(Object messageObject);

	/**
	 * Initialize a process context which will contain a list of errors and other stuff. The message should also be created here if not given yet (recycling)
	 *
	 * @param messageObject The message object received from an Handle
	 * @return a new process context used for this message and this message only
	 */
	protected abstract C buildProcessContext(Object messageObject);

	protected final C getProcessContext() {
		return processContextThreadLocal.get();
	}

	/**
	 * Core method of an agent, this method processes the message object.
	 * It can raise errors during the process. Those errors will be matched to the dictionary to adapt the behavior of the process accordingly
	 *
	 * @param messageObject The message object received from an Handle
	 */
	protected abstract void process(Object messageObject);

	/**
	 * The process is in an invalid state, it has been rejected
	 */
	protected abstract void reject();

	/**
	 * The process is in a valid state, it has been accepted
	 */
	protected abstract void accept();

	/**
	 * Are the raised errors blocking the final step of the process?
	 */
	private boolean checkBlocking(IProcessingResult processingResult) {
		IMessageType messageType = dictionary.getMessageType(messageTypeName);
		if (processingResult.getState() == IProcessingResult.State.INVALID) {
			switch (processingResult.getErrorRecyclingKind()) {
				case AUTOMATIC:
					notifyMessageStatus(MessageStatus.TO_RECYCLE_AUTOMATICALLY);
					break;
				case MANUAL:
					notifyMessageStatus(MessageStatus.TO_RECYCLE_MANUALLY);
					break;
				case NOT_RECYCLABLE:
					notifyMessageStatus(MessageStatus.REJECTED);
					break;
				default:
					throw new IllegalStateException("Status is INVALID but errorRecyclingKind is " + processingResult.getErrorRecyclingKind());
			}
		} else if (messageType.getMessageWay() == MessageWay.OUT) {
			notifyMessageStatus(MessageStatus.SENT);
		} else {
			notifyMessageStatus(MessageStatus.INTEGRATED);
		}

		return processingResult.getErrorRecyclingKind() != null && processingResult.getErrorRecyclingKind().getCriticity() > 0;
	}

	/**
	 * Add an error to the process
	 */
	protected final void addError(String errorCode, String attribute, String value) {
		processContextThreadLocal.get().getProcessErrorList().add(processErrorFactory.createProcessError(errorCode, attribute, value));
	}
}
