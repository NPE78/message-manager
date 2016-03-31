package com.synaptix.mm.server;

import java.util.Set;

/**
 * Created by NicolasP on 04/12/2015.
 */
public interface IServer {

	String HEARTBEAT = "heartbeat"; //$NON-NLS-1$

	String DISABLED = "disabled"; //$NON-NLS-1$

	/**
	 * Starts the server
	 */
	void start(String integFolder);

	/**
	 * Stops the server
	 */
	void stop();

	/**
	 * Is the server still running?
	 */
	boolean isRunning();

	/**
	 * Get a description of what is still running
	 */
	Set<String> runningSet();

}
