package com.talanlabs.mm.engine.model;

import com.talanlabs.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public final class DefaultProcessError implements IProcessError {

	private final String errorCode;

	private String attribute;

	private String value;

	public DefaultProcessError(String errorCode) {
		this(errorCode, null, null);
	}

	public DefaultProcessError(String errorCode, String attribute, String value) {
		super();

		this.errorCode = errorCode;

		setAttribute(attribute);
		setValue(value);
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
