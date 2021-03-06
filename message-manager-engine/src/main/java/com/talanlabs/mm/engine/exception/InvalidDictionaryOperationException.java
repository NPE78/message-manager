package com.talanlabs.mm.engine.exception;

import com.talanlabs.mm.engine.MMDictionary;

/**
 * This exception is raised when a subset dictionary is added twice in a {@link MMDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public final class InvalidDictionaryOperationException extends Exception {

	public InvalidDictionaryOperationException(String message) {
		super(message);
	}
}
