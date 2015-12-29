package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when a subset dictionary is added with an invalid name in a {@link MMDictionary}
 * Created by NicolasP on 29/12/2015.
 */
public final class InvalidDictionaryNameException extends RuntimeException {

	public InvalidDictionaryNameException(String message) {
		super(message);
	}
}
