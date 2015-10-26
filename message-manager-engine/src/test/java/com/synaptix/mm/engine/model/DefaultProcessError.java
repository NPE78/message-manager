package com.synaptix.mm.engine.model;

import com.synaptix.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultProcessError implements IProcessError {

	private final String errorCode;

	private final String attribute;

	private final String value;

	public DefaultProcessError(String errorCode, String attribute, String value) {
		super();

		this.errorCode = errorCode;
		this.attribute = attribute;
		this.value = value;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public String getValue() {
		return value;
	}
}
