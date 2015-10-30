package com.synaptix.mm.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.synaptix.mm.engine.IMMProcess;
import com.synaptix.mm.engine.MMEngine;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.engine.model.ProcessingResultBuilder;
import com.synaptix.mm.server.model.IProcessContext;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.pmgr.core.apis.Engine;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;

/**
 * Always use Guice to create an agent properly
 * An agent is the main class of a process. It is used by the Process Manager and {@link ProcessEngine#handle(String, Object)} method
 * Created by NicolasP on 23/10/2015.
 */
public abstract class AbstractMMAgent<C extends IProcessContext> implements ProcessingChannel.Agent, IMMProcess {

	private static final Log LOG = LogFactory.getLog(AbstractMMAgent.class);

	private final String messageTypeName;

	/**
	 * Since the agent is used by many threads at the same time, we need to use a thread local to ensure the process context is thread-safe
	 */
	private final ThreadLocal<C> processContextThreadLocal;

	@Inject
	private IProcessErrorFactory processErrorFactory;

	@Inject
	private MMEngine engine;

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

	/**
	 * If the message concerns this agent, uses the mm engine to track any progress during the process
	 */
	public final IProcessingResult doWork(Object messageObject) {
		if (isConcerned(messageObject)) {
			C processContext = buildProcessContext(messageObject);
			processContextThreadLocal.set(processContext);
			processContext.setProcessErrorList(new ArrayList<>());
			return engine.start(messageObject, this);
		}
		return ProcessingResultBuilder.rejectDefinitely(); // if the message does not concern this agent, we reject it
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
	protected abstract boolean isConcerned(Object messageObject);

	/**
	 * Initialize a process context which will contain a list of errors and other stuff. The message should also be created here if not given yet (recycling)
	 *
	 * @param messageObject The message object received from an Handle
	 * @return a new process context used for this message and this message only
	 */
	protected abstract C buildProcessContext(Object messageObject);

	/**
	 * Get the process context. Uses the thread local because of the multi-threading configuration
	 */
	protected final C getProcessContext() {
		return processContextThreadLocal.get();
	}

	/**
	 * Add an error to the process
	 */
	protected final void addError(String errorCode, String attribute, String value) {
		processContextThreadLocal.get().getProcessErrorList().add(processErrorFactory.createProcessError(errorCode, attribute, value));
	}

	@Override
	public List<IProcessError> getProcessErrorList() {
		return processContextThreadLocal.get().getProcessErrorList();
	}
}
