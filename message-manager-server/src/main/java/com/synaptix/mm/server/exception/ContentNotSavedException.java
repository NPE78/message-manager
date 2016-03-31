package com.synaptix.mm.server.exception;

/**
 * Created by NicolasP on 18/12/2015.
 */
public class ContentNotSavedException extends Exception {

	public ContentNotSavedException(String message) {
		super(message);
	}

	public ContentNotSavedException(String message, Throwable cause) {
		super(message, cause);
	}
}
