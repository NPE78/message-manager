package com.synaptix.mm.engine.model;

import java.util.Date;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
class ProcessingResult implements IProcessingResult {

	private IProcessingResult.State state;

	private ErrorRecyclingKind errorRecyclingKind;

	private Date nextProcessingDate;

	ProcessingResult() {
	}

	public IProcessingResult.State getState() {
		return state;
	}

	public void setState(IProcessingResult.State state) {
		this.state = state;
	}

	public ErrorRecyclingKind getErrorRecyclingKind() {
		return errorRecyclingKind;
	}

	public void setErrorRecyclingKind(ErrorRecyclingKind errorRecyclingKind) {
		this.errorRecyclingKind = errorRecyclingKind;
	}

	public Date getNextProcessingDate() {
		return nextProcessingDate;
	}

	public void setNextProcessingDate(Date nextProcessingDate) {
		this.nextProcessingDate = nextProcessingDate;
	}
}
