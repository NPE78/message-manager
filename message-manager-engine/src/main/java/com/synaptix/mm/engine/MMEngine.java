package com.synaptix.mm.engine;

import com.google.inject.Inject;
import com.synaptix.mm.engine.exception.DictionaryAlreadyDefinedException;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * This class represents an instance of a message manager engine. It encapsulates a whole process
 * Created by NicolasP on 30/10/2015.
 */
public final class MMEngine {

	@Inject
	private MMDictionary dictionary;

	/**
	 * Create the unique instance of the message manager engine
	 */
	@Inject
	public MMEngine() {
		super();
	}

	public void setDictionary(MMDictionary dictionary) {
		if (this.dictionary != null) {
			throw new DictionaryAlreadyDefinedException("A main dictionary has already been defined");
		}
		this.dictionary = dictionary;
	}

	public IProcessingResult start(Object messageObject, IMMProcess process) {
		process.notifyMessageStatus(MessageStatus.IN_PROGRESS);
		process.process(messageObject);

		SubDictionary subDictionary = process.getValidationDictionary(dictionary);
		IProcessingResult processingResult = subDictionary.getProcessingResult(process.getMessageTypeName(), process.getProcessErrorList());

		if (checkBlocking(processingResult, process)) {
			process.reject();
		} else {
			process.accept();
		}
		return processingResult;
	}

	/**
	 * Of all the errors raised until here, are they blocking the process?
	 */
	private boolean checkBlocking(IProcessingResult processingResult, IMMProcess process) {
		IMessageType messageType = dictionary.getMessageType(process.getMessageTypeName());
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
		} else if (messageType.getMessageWay() == MessageWay.OUT) {
			process.notifyMessageStatus(MessageStatus.SENT);
		} else {
			process.notifyMessageStatus(MessageStatus.INTEGRATED);
		}

		return processingResult.getErrorRecyclingKind() != null && processingResult.getErrorRecyclingKind().getCriticity() > 0;
	}
}
