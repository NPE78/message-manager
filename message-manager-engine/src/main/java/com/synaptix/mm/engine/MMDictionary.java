package com.synaptix.mm.engine;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;
import com.synaptix.mm.engine.exception.MessageTypeAlreadyDefinedException;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.exception.UnknownMessageTypeException;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.engine.model.ProcessingResultBuilder;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * This object contains the configuration of each message type.
 * Created by NicolasP on 22/10/2015.
 */
public final class MMDictionary {

	private final Map<String, IMessageType> messageTypeMap;

	private final Map<String, List<IErrorType>> errorTypeMap;

	/**
	 * Use Guice to create this class
	 */
	@Inject
	public MMDictionary() {
		super();

		this.messageTypeMap = new HashMap<>();
		this.errorTypeMap = new HashMap<>();
	}

	/**
	 * Add a message type and returns the list of errors which will be used to compute the processing result
	 */
	public List<IErrorType> addMessageType(IMessageType messageType) {
		String messageTypeName = messageType.getName();
		checkConflict(messageTypeName);

		messageTypeMap.put(messageTypeName, messageType);

		List<IErrorType> errorTypeList = new ArrayList<>();
		errorTypeMap.put(messageTypeName, errorTypeList);
		return errorTypeList;
	}

	/**
	 * Add a message type and a list of errors. If the message type already exists, throws an exception.
	 */
	public void addMessageType(IMessageType messageType, List<IErrorType> errorTypeList) {
		String messageTypeName = messageType.getName();
		checkConflict(messageTypeName);

		messageTypeMap.put(messageTypeName, messageType);
		errorTypeMap.put(messageTypeName, errorTypeList != null ? errorTypeList : new ArrayList<>());
	}

	private void checkConflict(String messageTypeName) {
		if (messageTypeMap.containsKey(messageTypeName)) {
			throw new MessageTypeAlreadyDefinedException("The message type " + messageTypeName + " already exists!");
		}
	}

	/**
	 * Get the processing result given a message type and a list of errors raised during the process.
	 * It uses the dictionnary to determine whether the process is valid or invalid, computes the recycling kind according to the configuration and if needed a next processing date
	 */
	public IProcessingResult getProcessingResult(String messageTypeName, List<IProcessError> errorList) {
		if (errorList == null || errorList.isEmpty()) {
			return ProcessingResultBuilder.accept();
		}

		List<IErrorType> errorTypeList = errorTypeMap.get(messageTypeName);

		Worst worst = new Worst();

		errorList.forEach(s -> {
			Optional<IErrorType> first = errorTypeList.stream().filter(errorType -> errorType.getCode().equals(s.getErrorCode())).findFirst();
			if (!first.isPresent()) {
				throw new UnknownErrorException("Error code " + s.getErrorCode() + " not found in Message Type " + messageTypeName);
			}
			updateWorst(worst, first.get());
		});

		return buildProcessingResult(worst);
	}

	private void updateWorst(Worst worst, IErrorType errorType) {
		worst.errorRecyclingKind = ErrorRecyclingKind.getWorst(errorType.getRecyclingKind(), worst.errorRecyclingKind);
		worst.delay = Math.max(errorType.getNextRecyclingDuration() != null ? errorType.getNextRecyclingDuration() : 0, worst.delay != null ? worst.delay : 0);
	}

	private IProcessingResult buildProcessingResult(Worst worst) {
		switch (worst.errorRecyclingKind) {
			case AUTOMATIC:
				Instant nextProcessingDate = Instant.now();
				nextProcessingDate.plus(worst.delay, ChronoUnit.MINUTES);
				return ProcessingResultBuilder.rejectAutomatically(nextProcessingDate);
			case MANUAL:
				return ProcessingResultBuilder.rejectManually();
			case NOT_RECYCLABLE:
				return ProcessingResultBuilder.rejectDefinitely();
			default:
				// case of the WARNING enum value
				return ProcessingResultBuilder.acceptWithWarning();
		}
	}

	/**
	 * Get the message type corresponding to the given message type name. Throws an UnknownMessageTypeException if the message type does not exist
	 */
	public IMessageType getMessageType(String messageTypeName) {
		IMessageType messageType = messageTypeMap.get(messageTypeName);
		if (messageType == null) {
			throw new UnknownMessageTypeException("The message type " + messageTypeName + " does not exist!");
		}
		return messageType;
	}

	private class Worst {

		ErrorRecyclingKind errorRecyclingKind;

		Integer delay;

	}
}
