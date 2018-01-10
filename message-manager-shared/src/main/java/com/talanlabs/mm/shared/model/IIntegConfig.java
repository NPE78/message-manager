package com.talanlabs.mm.shared.model;

/**
 * Created by NicolasP on 31/03/2016.
 */
public interface IIntegConfig {

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
