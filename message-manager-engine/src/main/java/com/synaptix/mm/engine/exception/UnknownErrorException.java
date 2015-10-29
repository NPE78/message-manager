package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when an error was expected for a message type but has not been found
 * See {@link MMDictionary}
 * Created by NicolasP on 23/10/2015.
 */
public class UnknownErrorException extends RuntimeException {

	public UnknownErrorException(String message) {
		super(message);
	}
}
