package com.synaptix.mm.shared.model;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IErrorType {

	/**
	 * The code of the error (unique for a message type)
	 */
	String getCode();

	/**
	 * The recycling kind induced by the error
	 */
	ErrorRecyclingKind getRecyclingKind();

	/**
	 * The error will ask the message to be recycled after this duration (in minutes) if the message has to be recycled automatically
	 */
	Integer getNextRecyclingDuration();

}
