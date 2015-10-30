package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when a message type was expected but has not been found
 * See {@link MMDictionary}
 * Created by NicolasP on 23/10/2015.
 */
public final class UnknownMessageTypeException extends RuntimeException {

	public UnknownMessageTypeException(String message) {
		super(message);
	}
}
