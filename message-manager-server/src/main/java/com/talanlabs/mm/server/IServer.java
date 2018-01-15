package com.talanlabs.mm.server;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.model.AbstractMMFlux;
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
     * Sets the process error factory which will be passed to the process manager engine when starting up the server<br>
     * The process error factory MUST BE set before starting the server or it won't be used
     */
    void setProcessErrorFactory(IProcessErrorFactory processErrorFactory);

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

    /**
     * Send a message asynchronously to an agent of given class (using its channel)
     * @param agentClass agent class to use
     * @param message message to send
     * @param <F> type of message to send to an agent which manages this type of message
     */
    <F extends AbstractMMFlux> void handle(Class<? extends AbstractMMAgent<F>> agentClass, F message);
}
