package com.synaptix.mm.shared.model;

import java.util.Date;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.ErrorStatus;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IError {

	/**
	 * The type of the error
	 */
	public IErrorType getType();

	/**
	 * The recycling kind induced by the error
	 */
	public ErrorRecyclingKind getRecyclingKind();

	/**
	 * The status of the error
	 */
	public ErrorStatus getErrorStatus();

	/**
	 * The error will ask the message to be recycled starting the given date (or after if another error has a superior next processing date) if the message has to be recycled automatically
	 */
	public Date getNextProcessingDate();

}
