package com.talanlabs.mm.server.exception;

/**
 * Created by NicolasP on 18/12/2015.
 */
public class ContentNotFetchedException extends Exception {

	public ContentNotFetchedException(String message) {
		super(message);
	}

	public ContentNotFetchedException(String message, Throwable cause) {
		super(message, cause);
	}
}
