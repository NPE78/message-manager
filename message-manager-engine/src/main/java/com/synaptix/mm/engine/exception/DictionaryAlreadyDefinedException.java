package com.synaptix.mm.engine.exception;

import com.synaptix.mm.engine.MMDictionary;

/**
 * This exception is raised when a subset dictionary is added twice in a {@link MMDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public final class DictionaryAlreadyDefinedException extends Exception {

	public DictionaryAlreadyDefinedException(String message) {
		super(message);
	}
}
