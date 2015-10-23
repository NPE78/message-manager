package com.synaptix.mm.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
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

	@Inject(optional = true)
	private IProcessErrorFactory errorFactory;

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

		if (errorTypeList == null) {
			errorTypeList = new ArrayList<>();
		}

		messageTypeMap.put(messageTypeName, messageType);
		errorTypeMap.put(messageTypeName, errorTypeList);
	}

	private void checkConflict(String messageTypeName) {
		if (messageTypeMap.containsKey(messageTypeName)) {
			throw new RuntimeException("The message type " + messageTypeName + " already exists!");
		}
	}

//	public IError createError(String messageTypeName, String errorCode) {
//		if (errorFactory == null) {
//			throw new NullPointerException("IProcessErrorFactory has no implementation or implementation has not be binded correctly.");
//		}
//		return errorFactory.createError(messageTypeName, errorCode);
//	}

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
		ErrorRecyclingKind errorRecyclingKind = null;
		Date nextProcessingDate = null;

		errorList.forEach(s -> {
			Optional<IErrorType> first = errorTypeList.stream().filter(errorType -> errorType.getCode().equals(s.getErrorCode())).findFirst();
			if (!first.isPresent()) {
				throw new RuntimeException("Error code " + s + " not found in Message Type " + messageTypeName);
			}
			IErrorType errorType = first.get();
			worst.errorRecyclingKind = ErrorRecyclingKind.getWorst(errorType.getRecyclingKind(), worst.errorRecyclingKind);
			worst.delay = Math.max(errorType.getNextRecyclingDuration() != null ? errorType.getNextRecyclingDuration() : 0, worst.delay != null ? worst.delay : 0);
		});

		switch (worst.errorRecyclingKind) {
//			case WARNING:
//				return ProcessingResultBuilder.accept();
			case AUTOMATIC:
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, worst.delay);
				return ProcessingResultBuilder.reject(ErrorRecyclingKind.AUTOMATIC, cal.getTime());
			case MANUAL:
				return ProcessingResultBuilder.reject(ErrorRecyclingKind.MANUAL, null);
			case NOT_RECYCLABLE:
				return ProcessingResultBuilder.reject(ErrorRecyclingKind.NOT_RECYCLABLE, null);
			default:
				return ProcessingResultBuilder.accept();
		}
	}

	private class Worst {

		ErrorRecyclingKind errorRecyclingKind;

		Integer delay;

	}

//	public IMessageType findMessageType(String messageTypeName) {
//		IMessageType messageType = messageTypeMap.get(messageTypeName);
//		if (messageType == null) {
//			throw new RuntimeException("Message Type not found: " + messageTypeName);
//		}
//		return messageType;
//	}
//
//	public IErrorType findErrorType(String messageTypeName, String errorCode) {
//		List<IErrorType> errorTypeList = errorTypeMap.get(messageTypeName);
//		Optional<IErrorType> first = errorTypeList.stream().filter(errorType -> errorType.getCode().equals(errorCode)).findFirst();
//		if (!first.isPresent()) {
//			throw new RuntimeException("Error code " + errorCode + " not found in Message Type " + messageTypeName);
//		}
//		return first.get();
//	}
}