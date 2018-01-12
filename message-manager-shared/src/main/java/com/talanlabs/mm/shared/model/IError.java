package com.talanlabs.mm.shared.model;

import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.ErrorStatus;
import java.time.Instant;

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
