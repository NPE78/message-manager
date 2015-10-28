package com.synaptix.mm.engine.factory;

import com.synaptix.mm.shared.model.IProcessError;

/**
 * To build errors, this interface has to be binded to an implementation by using guice
 * Created by NicolasP on 22/10/2015.
 */
public interface IProcessErrorFactory {

	/**
	 * Create a process error with given errorCode, attribute and value. If needed, the other fields can be enriched after.
	 */
	IProcessError createProcessError(String errorCode, String attribute, String value);

}
