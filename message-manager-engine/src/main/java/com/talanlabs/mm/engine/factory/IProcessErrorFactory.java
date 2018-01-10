package com.talanlabs.mm.engine.factory;

import com.talanlabs.mm.shared.model.IProcessError;

/**
 * This interface is used to build process errors
 */
public interface IProcessErrorFactory {

	/**
	 * Create a process error with given errorCode, attribute and value. If needed, the other fields can be enriched after.
	 */
	IProcessError createProcessError(String errorCode);

}
