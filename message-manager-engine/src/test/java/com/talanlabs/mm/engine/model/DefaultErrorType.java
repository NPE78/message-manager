package com.talanlabs.mm.engine.model;

import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Default error type implementation
 */
public class DefaultErrorType implements IErrorType {

	private final String code;

	private final ErrorRecyclingKind errorRecyclingKind;

	private final Integer nextRecyclingDuration;

    /**
     * Builds an error type with given unique code, and recycling kind
     * The default next recycling duration is 60 minutes
     */
	public DefaultErrorType(String code, ErrorRecyclingKind errorRecyclingKind) {
		this(code, errorRecyclingKind, 60);
	}

    /**
     * Builds an error type with given unique code, and recycling kind and next recycling duration (in minutes)
     */
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
