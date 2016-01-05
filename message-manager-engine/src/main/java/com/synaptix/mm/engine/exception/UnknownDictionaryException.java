package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when trying to get a subset dictionary from a dictionary
 * See {@link MMDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public final class UnknownDictionaryException extends Exception {

	public UnknownDictionaryException(String message) {
		super(message);
	}
}
