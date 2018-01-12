package com.talanlabs.mm.server;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.messages.probe.ProbeAgent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.util.List;
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

    private IProcessErrorFactory processErrorFactory;

    /**
     * @param engineUuid This is the id of the process manager to create
     * @param errorPath  Path where the remaining messages will be stored when shutting down
     */
    public MMServer(String engineUuid, File errorPath) throws BaseEngineCreationException {
        super();

        logService = LogManager.getLogService(getClass());

        logService.info(() -> "New server: " + engineUuid);

        this.engine = ProcessManager.getInstance().createEngine(engineUuid, errorPath);

        this.timeoutSeconds = 2 * 60;
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
        this.processErrorFactory = processErrorFactory;
    }

    /**
     * Launches the process manager: activates channels, init agents
     */
    @Override
    public void start(String integFolder) {
        logService.info(() -> "START MMServer {0} on {1}", engine.getUuid(), integFolder);

        try {
            setBaseDir(integFolder);
        } catch (IllegalAccessException e) {
            logService.error(() -> "Error when initializing file system manager", e);
        }

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
            }
        } catch (FileSystemException e) {
            logService.error(() -> "Error when initializing file system manager", e);
        }
    }

    private void launchEngine() {
        if (processErrorFactory != null) {
            MMEngineAddon.register(engine.getUuid(), processErrorFactory);
        } else {
            MMEngineAddon.register(engine.getUuid());
        }
        engine.activateChannels();

        initAgents();

        this.started = true;
    }

    private void initAgents() {
        List<? extends AbstractMMAgent<?>> agentStream = getAgentStream();
        final CountDownLatch cdl = new CountDownLatch(agentStream.size());
        agentStream.forEach(a -> askToIdentify(a, cdl));

        try {
            cdl.await();
        } catch (InterruptedException e) {
            logService.error(() -> "Init agents interrupted for {0}", e, engine.getUuid());
        }

        logService.info(() -> "End of identification");
    }

    private void askToIdentify(AbstractMMAgent<?> agentProcess, CountDownLatch cdl) {
        new Thread(() -> {
            try {
                logService.info(() -> "Sending identification message to {0}", agentProcess.getClass().getSimpleName());
                agentProcess.identificate();
            } finally {
                cdl.countDown();
            }
        }).start();
    }

    /**
     * Returns a stream of agents known by MM
     */
    private List<? extends AbstractMMAgent<?>> getAgentStream() {
        return getProcessingChannelStream()
                .map(ProcessingChannel::getAgent).filter(AbstractMMAgent.class::isInstance).map(agent -> (AbstractMMAgent<?>) agent).collect(Collectors.toList());
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
        engine.shutdown();
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
}
