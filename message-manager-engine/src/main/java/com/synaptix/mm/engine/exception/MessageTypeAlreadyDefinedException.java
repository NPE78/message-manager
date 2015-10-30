package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when a message type is added twice in a {@link MMDictionary}
 * Created by NicolasP on 23/10/2015.
 */
public final class MessageTypeAlreadyDefinedException extends RuntimeException {

	public MessageTypeAlreadyDefinedException(String message) {
		super(message);
	}
}
