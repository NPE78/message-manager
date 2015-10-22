package com.synaptix.mm.shared.model.domain;

/**
 * Created by NicolasP on 22/10/2015.
 */
public enum ErrorStatus {

	/**
	 * The error is has been raised during the last injection of the message
	 */
	OPENED,
	/**
	 * The error has been raised once but has not been raised during the last injection
	 */
	CLOSED,
	/**
	 * The error has been marked as such by an user, which indicates the error won't block any integration of the message
	 */
	IGNORED;

}
