package com.talanlabs.mm.engine;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.shared.model.IMessage;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents an instance of a message manager engine. It encapsulates a whole process
 */
public final class MMEngine {

	private static final Log LOG = LogFactory.getLog(MMEngine.class);

	private IProcessErrorFactory processErrorFactory;

	/**
	 * Create the unique instance of the message manager engine
	 */
	public MMEngine() {
		super();
	}

	public void setProcessErrorFactory(IProcessErrorFactory processErrorFactory) {
		this.processErrorFactory = processErrorFactory;
	}

	public <F extends IMessage> IProcessingResult start(F message, IMMProcess<F> process, MMDictionary dictionary) {
		process.notifyMessageStatus(MessageStatus.IN_PROGRESS);

		process.start(); // notify the start of the process (for super transaction for instance)

		try {
			process.process(message);
		} catch (Exception e) {
			LOG.error("UNKNOWN_ERROR", e); //$NON-NLS-1$
			IProcessError processError = processErrorFactory.createProcessError("UNKNOWN_ERROR"); //$NON-NLS-1$
			process.getProcessErrorList().add(processError);
		}

		SubDictionary subDictionary = process.getValidationDictionary(dictionary);
		IProcessingResult processingResult = subDictionary.getProcessingResult(process.getProcessErrorList());

		boolean blocking = checkBlocking(processingResult);
		process.close(!blocking); // notify the end of the process (for super transaction for instance)

		if (blocking) {
			process.reject(processingResult.getErrorMap());
		} else {
			process.accept(processingResult.getErrorMap());
		}

		notifyMessageResult(processingResult, process);
		return processingResult;
	}

	/**
	 * Of all the errors raised until here, are they blocking the process?
	 */
	private boolean checkBlocking(IProcessingResult processingResult) {
		return processingResult.getErrorRecyclingKind() != null && processingResult.getErrorRecyclingKind().getCriticity() > 0;
	}

	private void notifyMessageResult(IProcessingResult processingResult, IMMProcess process) {
		if (processingResult.getState() == IProcessingResult.State.INVALID) {
			switch (processingResult.getErrorRecyclingKind()) {
				case AUTOMATIC:
					process.notifyMessageStatus(MessageStatus.TO_RECYCLE_AUTOMATICALLY, processingResult.getNextProcessingDate());
					break;
				case MANUAL:
					process.notifyMessageStatus(MessageStatus.TO_RECYCLE_MANUALLY);
					break;
				case NOT_RECYCLABLE:
					process.notifyMessageStatus(MessageStatus.REJECTED);
					break;
				default:
					throw new IllegalStateException("Status is INVALID but errorRecyclingKind is " + processingResult.getErrorRecyclingKind());
			}
		} else if (process.getMessageWay() == MessageWay.OUT) {
			process.notifyMessageStatus(MessageStatus.SENT);
		} else {
			process.notifyMessageStatus(MessageStatus.INTEGRATED);
		}
	}
}
