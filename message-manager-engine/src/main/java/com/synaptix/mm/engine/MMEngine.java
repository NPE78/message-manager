package com.synaptix.mm.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.synaptix.mm.engine.exception.DictionaryAlreadyDefinedException;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * This class represents an instance of a message manager engine. It encapsulates a whole process
 * Created by NicolasP on 30/10/2015.
 */
public final class MMEngine {

	private static final Log LOG = LogFactory.getLog(MMEngine.class);

	@Inject
	private IProcessErrorFactory processErrorFactory;

	private MMDictionary dictionary;

	/**
	 * Create the unique instance of the message manager engine
	 */
	@Inject
	public MMEngine() {
		super();
	}

	@Inject
	public void setDictionary(MMDictionary dictionary) throws DictionaryAlreadyDefinedException {
		if (this.dictionary != null) {
			throw new DictionaryAlreadyDefinedException("A main dictionary has already been defined");
		}
		this.dictionary = dictionary;
	}

	public IProcessingResult start(Object messageObject, IMMProcess process) {
		process.notifyMessageStatus(MessageStatus.IN_PROGRESS);
		try {
			process.process(messageObject);
		} catch (Exception e) {
			LOG.error("UNKNOWN_ERROR", e);
			IProcessError processError = processErrorFactory.createProcessError("UNKNOWN_ERROR");
			process.getProcessErrorList().add(processError);
		}

		SubDictionary subDictionary = process.getValidationDictionary(dictionary);
		IProcessingResult processingResult = subDictionary.getProcessingResult(process.getProcessErrorList());

		if (checkBlocking(processingResult, process)) {
			process.reject(processingResult.getErrorMap());
		} else {
			process.accept(processingResult.getErrorMap());
		}
		return processingResult;
	}

	/**
	 * Of all the errors raised until here, are they blocking the process?
	 */
	public boolean checkBlocking(IProcessingResult processingResult, IMMProcess process) {
		if (processingResult.getState() == IProcessingResult.State.INVALID) {
			switch (processingResult.getErrorRecyclingKind()) {
				case AUTOMATIC:
					process.notifyMessageStatus(MessageStatus.TO_RECYCLE_AUTOMATICALLY);
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

		return processingResult.getErrorRecyclingKind() != null && processingResult.getErrorRecyclingKind().getCriticity() > 0;
	}
}
