package com.synaptix.mm.shared.model.domain;

/**
 * Created by NicolasP on 22/10/2015.
 */
public enum ErrorRecyclingKind {

	/**
	 * The error doesn't prevent the message to be integrated
	 */
	WARNING,
	/**
	 * The error will require an automatic recycling, if no error (manual or not recyclable) has been raised
	 */
	AUTOMATIC,
	/**
	* The error will require a manual recycling, if no error (not recyclable) has been raised
	*/
	MANUAL,
	/**
	 * The error will definitely reject the message
	 */
	NOT_RECYCLABLE;

}
