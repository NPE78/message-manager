package com.synaptix.mm.engine.model;

import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class SimpleErrorType implements IErrorType {

	private ErrorRecyclingKind errorRecyclingKind;

	public SimpleErrorType(ErrorRecyclingKind errorRecyclingKind) {
		super();

		this.errorRecyclingKind = errorRecyclingKind;
	}

	@Override
	public String getCode() {
		return "SimpleErrorType";
	}

	@Override
	public ErrorRecyclingKind getRecyclingKind() {
		return errorRecyclingKind;
	}

	@Override
	public Integer getNextRecyclingDuration() {
		return 60;
	}
}
