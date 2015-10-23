package com.synaptix.mm.engine.model;

import java.util.Date;

import com.synaptix.mm.shared.model.IError;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.ErrorStatus;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class SimpleError implements IError {

	private  final IErrorType errorType;

	private final ErrorRecyclingKind errorRecyclingKind;

	private final ErrorStatus errorStatus;

	private final Date nextProcessingDate;

	public SimpleError(IErrorType errorType, ErrorRecyclingKind errorRecyclingKind, ErrorStatus errorStatus, Date nextProcessingDate) {
		super();

		this.errorType = errorType;
		this.errorRecyclingKind = errorRecyclingKind;
		this.errorStatus = errorStatus;
		this.nextProcessingDate = nextProcessingDate;
	}


	@Override
	public IErrorType getType() {
		return errorType;
	}

	@Override
	public ErrorRecyclingKind getRecyclingKind() {
		return errorRecyclingKind;
	}

	@Override
	public ErrorStatus getErrorStatus() {
		return errorStatus;
	}

	@Override
	public Date getNextProcessingDate() {
		return nextProcessingDate;
	}
}
