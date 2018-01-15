package com.talanlabs.mm.server;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.probe.ProbeAgent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * Launches an instance of the process manager
 */
public class MMServer implements IServer {

    private final LogService logService;

    private final Engine engine;

    private int timeoutSeconds;

    private boolean started;

    /**
     * @param engineUuid This is the id of the process manager to create
     * @param errorPath  Path where the remaining messages will be stored when shutting down
     */
    public MMServer(String engineUuid, String integFolder, File errorPath) throws BaseEngineCreationException {
        super();

        logService = LogManager.getLogService(getClass());
        logService.info(() -> "New server: " + engineUuid);

        try {
            setBaseDir(integFolder);
        } catch (IllegalAccessException e) {
            logService.error(() -> "Error when initializing file system manager", e);
        }

        this.engine = ProcessManager.getInstance().createEngine(engineUuid, errorPath);

        this.timeoutSeconds = 2 * 60;

        MMEngineAddon.register(engine.getUuid(), this);
    }

    /**
     * Changes the timeout, meaning the duration to wait for the agents to finish properly before killing the process manager
     *
     * @param timeoutSeconds timeout in seconds, implem is 2min
     */
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    private String getVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "local";
        }
        return version;
    }

    @Override
    public void setProcessErrorFactory(IProcessErrorFactory processErrorFactory) {
        MMEngineAddon.setProcessErrorFactory(engine.getUuid(), processErrorFactory);
    }

    /**
     * Launches the process manager: activates channels, init agents
     */
    @Override
    public void start() {
        logService.info(() -> "START MMServer {0}", engine.getUuid());

        String version = getVersion();
        logService.info(() -> "Version : " + version);

        launchEngine();
    }

    /**
     * Call setBaseDir prior to the start of the server
     *
     * @param integFolder Base path where all messages are stored
     * @throws IllegalAccessException an exception is thrown is the server is already started
     */
    public void setBaseDir(String integFolder) throws IllegalAccessException {
        if (started) {
            throw new IllegalAccessException("The integrator is already started, the main folder can't be changed!");
        }
        try {
            DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
            if (manager.getBaseFile() == null) {
                manager.setBaseFile(new File(integFolder));
                if (!manager.getBaseFile().exists()) {
                    manager.getBaseFile().createFolder();
                }
            } else {
                String path = manager.getBaseFile().getURL().getPath();
                logService.warn(() -> "VFS already has a base file: " + path);
            }
        } catch (FileSystemException e) {
            logService.error(() -> "Error when initializing file system manager", e);
        }
    }

    private void launchEngine() {
        engine.activateChannels();

        this.started = true;
    }

    private Stream<ProcessingChannel> getProcessingChannelStream() {
        return engine.getPluggedChannels().stream().filter(ProcessingChannel.class::isInstance).map(ProcessingChannel.class::cast);
    }

    private void stopAgent(ProbeAgent agent) {
        logService.info(() -> "Stopping " + agent.getChannel());
        agent.shutdown();
        logService.info(() -> "Stop sent to " + agent.getChannel());
    }

    /**
     * Stop the process manager and wait for all agents to finish.
     * Use {@link #setTimeoutSeconds} to change the timeout (default is 2min)
     */
    @Override
    public void stop() {
        if (!started) {
            logService.error(() -> "MMServer is not launched");
            return;
        }
        logService.info(() -> "STOP MMServer");

        getProcessingChannelStream().map(ProcessingChannel::getAgent)
                .filter(ProbeAgent.class::isInstance).map(ProbeAgent.class::cast).forEach(this::stopAgent);
        logService.info(() -> "End of probe agents");

        started = false;

        engine.shutdown();

        logService.info(() -> "Stop called: stopped, waiting for processes to finish, " + timeoutSeconds + " seconds max");

        waitForStop();

        logService.info(() -> "MMServer finished");
    }

    private void waitForStop() {
        final CountDownLatch cdl = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            while (isRunning()) {
                waitSec();
            }
            cdl.countDown();
        });
        thread.setDaemon(false);
        thread.start();

        try {
            if (cdl.await(timeoutSeconds, TimeUnit.SECONDS)) {
                logService.info(() -> "Stop called: stopped in time");
            } else {
                logService.info(() -> "Stop called: timeout");
                logService.info(() -> runningSet().toString());
            }
        } catch (InterruptedException e) {
            logService.error(() -> "Stop called", e);
        }
    }

    private void waitSec() {
        try {
            CountDownLatch cdl = new CountDownLatch(1);
            cdl.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logService.error(() -> "Stop waiter interrupted", e);
        }
    }

    /**
     * Returns true if there is at least one agent which is running
     */
    @Override
    public final boolean isRunning() {
        return getProcessingChannelStream().anyMatch(processingChannel -> processingChannel.getNbWorking() > 0);
    }

    /**
     * Returns true if the server is started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns a set of agents which are running at that moment
     */
    @Override
    public final Set<String> runningSet() {
        return getProcessingChannelStream().filter(processingChannel -> processingChannel.getNbWorking() > 0)
                .map(ProcessingChannel::getName).collect(Collectors.toSet());
    }

    @Override
    public <F extends AbstractMMFlux> void handle(Class<? extends AbstractMMAgent<F>> agentClass, F message) {
        ProcessManager.handle(engine.getUuid(), agentClass.getSimpleName(), message);
    }

    public static <F extends AbstractMMFlux> void handle(String engineUuid, Class<? extends AbstractMMAgent<F>> agentClass, F message) {
        ProcessManager.handle(engineUuid, agentClass.getSimpleName(), message);
    }
}
