package com.synaptix.mm.server;

import java.util.Set;

/**
 * Created by NicolasP on 04/12/2015.
 */
public interface IServer {

	/**
	 * Starts the server
	 */
	void start();

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
