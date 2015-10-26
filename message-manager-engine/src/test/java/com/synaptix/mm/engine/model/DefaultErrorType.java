package com.synaptix.mm.engine.model;

import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultErrorType implements IErrorType {

	private final String code;

	private final ErrorRecyclingKind errorRecyclingKind;

	private final Integer nextRecyclingDuration;

	public DefaultErrorType(String code, ErrorRecyclingKind errorRecyclingKind) {
		this(code, errorRecyclingKind, 60);
	}

	public DefaultErrorType(String code, ErrorRecyclingKind errorRecyclingKind, Integer nextRecyclingDuration) {
		super();

		this.code = code;
		this.errorRecyclingKind = errorRecyclingKind;
		this.nextRecyclingDuration = nextRecyclingDuration;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public ErrorRecyclingKind getRecyclingKind() {
		return errorRecyclingKind;
	}

	@Override
	public Integer getNextRecyclingDuration() {
		return nextRecyclingDuration;
	}
}
