package com.synaptix.mm.engine.factory;

import com.synaptix.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IProcessErrorFactory {

	/**
	 * Create a process error with given errorCode, attribute and value. The other fields can be enriched after.
	 */
	public IProcessError createProcessError(String errorCode, String attribute, String value);

}
