package com.talanlabs.mm.engine.exception;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.shared.model.IProcessError;

/**
 * This exception is raised when an error was expected for a message type but has not been found
 * See {@link MMDictionary}
 * Created by NicolasP on 23/10/2015.
 */
public final class UnknownErrorException extends Exception {

	private final IProcessError processError;

	public UnknownErrorException(String message) {
		this(message, null);
	}

	public UnknownErrorException(String message, Throwable cause) {
		this(null, message, cause);
	}

	public UnknownErrorException(IProcessError processError, String message) {
		this(processError, message, null);
	}

	public UnknownErrorException(IProcessError processError, String message, Throwable cause) {
		super(message, cause);

		this.processError = processError;
	}

	public IProcessError getProcessError() {
		return processError;
	}
}
