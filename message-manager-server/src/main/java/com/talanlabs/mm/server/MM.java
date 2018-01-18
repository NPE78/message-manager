package com.talanlabs.mm.server;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Launches an instance of the message manager server
 */
public class MM implements IServer {

    private final LogService logService;

    private final Engine engine;

    private int timeoutSeconds;

    private boolean started;

    /**
     * @param engineUuid  This is the id of the process manager to create
     * @param integFolder Base path where all messages folders are stored
     * @param errorPath   Path where the remaining messages will be stored when shutting down
     * @throws FileSystemException Thrown if an error occurred when initializing the file system manager
     */
    public MM(String engineUuid, String integFolder, File errorPath) throws BaseEngineCreationException, FileSystemException {
        super();

        logService = LogManager.getLogService(getClass());
        logService.info(() -> "New server: " + engineUuid);

        setBaseDir(integFolder);

        this.engine = PM.get().createEngine(engineUuid, errorPath);

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
     * Initialize the message manager server "virtual file system"
     *
     * @param integFolder Base path where all messages folders are stored
     * @throws FileSystemException Thrown if an error occurred when initializing the file system manager
     */
    private void setBaseDir(String integFolder) throws FileSystemException {
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
    }

    private void launchEngine() {
        engine.activateChannels();

        this.started = true;
    }

    private Stream<ProcessingChannel> getProcessingChannelStream() {
        return engine.getPluggedChannels().stream().filter(ProcessingChannel.class::isInstance).map(ProcessingChannel.class::cast);
    }

    /**
     * Stop the process manager and wait for all agents to finish.
     * Use {@link #setTimeoutSeconds} to change the timeout (default is 2min)
     */
    @Override
    public void stop() {
        if (!isStarted()) {
            logService.error(() -> "MMServer is not launched");
            return;
        }
        logService.info(() -> "STOP MMServer");

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

    /**
     * Send a message asynchronously to an agent of given class (using its channel)
     * @param message message to send (this message contains the information of the engine on which it has to be processed and the channel, using its name)
     * @param <F> type of message to send to an agent which manages this type of message
     */
    public static <F extends AbstractMMFlux> void handle(F message) {
        PM.handle(message.getEngineUuid(), message.getName(), message);
    }
}
