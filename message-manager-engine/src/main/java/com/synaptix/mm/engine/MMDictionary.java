package com.synaptix.mm.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.synaptix.mm.engine.exception.MessageTypeAlreadyDefinedException;
import com.synaptix.mm.engine.exception.UnknownMessageTypeException;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IMessageType;

/**
 * This object contains the configuration of each message type.
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * Created by NicolasP on 22/10/2015.
 */
public final class MMDictionary extends SubDictionary {

	private final Map<String, IMessageType> messageTypeMap;

	/**
	 * Use Guice to create this class. The main dictionary is unique
	 */
	@Inject
	public MMDictionary() {
		super("MAIN", null);

		this.messageTypeMap = new HashMap<>();
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
			throw new MessageTypeAlreadyDefinedException("The message type '" + messageTypeName + "' already exists!");
		}
	}

	/**
	 * Get the message type corresponding to the given message type name. Throws an {@link UnknownMessageTypeException} if the message type does not exist
	 */
	public IMessageType getMessageType(String messageTypeName) {
		IMessageType messageType = messageTypeMap.get(messageTypeName);
		if (messageType == null) {
			throw new UnknownMessageTypeException("The message type '" + messageTypeName + "' does not exist!");
		}
		return messageType;
	}
}
