package com.synaptix.mm.shared.model;

/**
 * Created by NicolasP on 31/03/2016.
 */
public interface IIntegConfig {

	/**
	 * The main folder of the integrator, where flux are stored
	 */
	String getIntegFolder();

	/**
	 * The host of the integrator server
	 */
	String getIntegHost();

	/**
	 * The port of the integrator server
	 */
	int getIntegPort();

	/**
	 * The name of the application (on tomcat for instance)
	 */
	String getIntegApplicationName();

}
