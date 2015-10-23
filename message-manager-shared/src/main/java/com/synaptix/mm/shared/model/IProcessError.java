package com.synaptix.mm.shared.model;

/**
 * Created by NicolasP on 23/10/2015.
 */
public interface IProcessError {

	/**
	 * The code of the error which will be used to determine the recycling kind according to the current configuration
	 */
	public String getErrorCode();

	/**
	 * The attribute of the error, which represents the reason why the error has been raised
	 */
	public String getAttribute();

	/**
	 * The value of the error, which is associated to the attribute
	 */
	public String getValue();

}
