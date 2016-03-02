package com.synaptix.mm.shared.model;

import java.io.Serializable;

/**
 * Created by NicolasP on 23/10/2015.
 */
public interface IProcessError extends Serializable {

	/**
	 * The code of the error which will be used to determine the recycling kind according to the current configuration
	 */
	String getErrorCode();

}
