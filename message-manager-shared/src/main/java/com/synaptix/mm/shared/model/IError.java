package com.synaptix.mm.shared.model;

import java.time.Instant;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.ErrorStatus;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IError {

	/**
	 * The type of the error
	 */
	IErrorType getType();

	/**
	 * The recycling kind induced by the error
	 */
	ErrorRecyclingKind getRecyclingKind();

	/**
	 * The status of the error
	 */
	ErrorStatus getErrorStatus();

	/**
	 * The error will ask the message to be recycled starting the given date (or after if another error has a superior next processing date) if the message has to be recycled automatically
	 */
	Instant getNextProcessingDate();

}
