package com.synaptix.mm.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synaptix.pmgr.core.apis.ChannelSlot;
import com.synaptix.pmgr.core.apis.PluggableChannel;
import com.synaptix.pmgr.core.lib.BaseEngine;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;
import com.synaptix.pmgr.core.lib.ProcessingChannel.Agent;
import com.synaptix.pmgr.core.lib.probe.CronAgent;
import com.synaptix.pmgr.core.lib.probe.HeartbeatAgent;
import com.synaptix.pmgr.plugin.GuicePluginManager;
import com.synaptix.toolkits.properties.PropertiesKit;

/**
 * Launches an instance of the process manager
 * Created by NicolasP on 28/10/2015.
 */
public class MMServer implements IServer {

	private static final Log LOG = LogFactory.getLog(MMServer.class);

	private final String trmt;

	private final GuicePluginManager guicePluginManager;

	private int timeoutSeconds;

	private boolean started;

	/**
	 * Use Guice to create this class.
	 *
	 * @param trmt This is the id of the process manager to create
	 */
	@Inject
	public MMServer(@Named("trmt") String trmt, GuicePluginManager guicePluginManager) {
		super();

		LOG.info("New server: " + trmt);

		this.trmt = trmt;
		this.guicePluginManager = guicePluginManager;

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

	/**
	 * Launches the process manager
	 */
	@Override
	public void start(String integFolder) {
		LOG.info("START MMServer");

		try {
			DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
			if (manager.getBaseFile() == null) {
				manager.setBaseFile(new File(integFolder));
				if (!manager.getBaseFile().exists()) {
					manager.getBaseFile().createFolder();
				}
			}
		} catch (FileSystemException e) {
			LOG.error("Error when initializing file system manager", e);
		}

		String version = getVersion();
		LOG.info("Version : " + version);

		launchEngine();
	}

	private void launchEngine() {
		Properties properties = PropertiesKit.load("process_engine.properties", ProcessEngine.class, true);
		guicePluginManager.initProcessManager(LOG, trmt, properties);

		initAgents();

		initGates();

		this.started = true;
	}

	private void initAgents() {
		List<AbstractMMAgent<?>> agentList = new ArrayList<>();
		Set<Class<?>> channelSet = new HashSet<>();
		ProcessEngine.getInstance().getChannels().stream().filter(channelSlot -> BaseEngine.ChannelSlotImpl.class.isAssignableFrom(channelSlot.getClass())).forEach(channelSlot -> {
			BaseEngine.ChannelSlotImpl channelSlotImpl = (BaseEngine.ChannelSlotImpl) channelSlot;
			if (ProcessingChannel.class.isAssignableFrom(channelSlotImpl.getPluggedChannel().getClass())) {
				ProcessingChannel processingChannel = (ProcessingChannel) channelSlotImpl.getPluggedChannel();
				Class<? extends ProcessingChannel.Agent> clazz = processingChannel.getAgent().getClass();
				if (AbstractMMAgent.class.isAssignableFrom(clazz) && !channelSet.contains(clazz)) {
					channelSet.add(clazz);
					agentList.add((AbstractMMAgent<?>) processingChannel.getAgent());
				}
			}
		});
		final CountDownLatch cdl = new CountDownLatch(agentList.size());
		List<Thread> threadList = new ArrayList<>();
		for (final AbstractMMAgent<?> agentProcess : agentList) {
			Thread thread = new Thread(() -> {
				try {
					LOG.info(String.format("Sending identification message to %s", agentProcess.getClass().getSimpleName()));
					agentProcess.identificate();
				} finally {
					cdl.countDown();
				}
			});
			threadList.add(thread);
		}

		threadList.forEach(Thread::start);
		try {
			cdl.await();
		} catch (InterruptedException e) {
			LOG.error("Interrupted", e);
		}

		LOG.info("End of identification");
	}

	private void initGates() {
		guicePluginManager.initGates(LOG);

		String heartbeat = System.getProperty(HEARTBEAT);
		if (heartbeat != DISABLED) {
			guicePluginManager.initHeartbeats();
		}
	}

	/**
	 * Stop the process manager and wait for all agents to finish.
	 * Use {@link #setTimeoutSeconds} to change the timeout (default is 2min)
	 */
	@Override
	public void stop() {
		if (!started) {
			LOG.error("MMServer is not launched");
			return;
		}
		LOG.info("STOP MMServer");

		Collection<ChannelSlot> channels = ProcessEngine.getInstance().getChannels();
		for (ChannelSlot channelSlot : channels) {
			if (channelSlot.getPluggedChannel() instanceof ProcessingChannel) {
				ProcessingChannel processingChannel = (ProcessingChannel) channelSlot.getPluggedChannel();
				Agent agent = processingChannel.getAgent();
				if ((agent instanceof HeartbeatAgent) || (agent instanceof CronAgent)) {
					LOG.info("Stopping " + channelSlot.getName());
					ProcessEngine.handle(channelSlot.getName(), "STOP");
					LOG.info("Stop sent to " + channelSlot.getName());
				}
			} else {
				LOG.info(channelSlot.getName() + " is not a processing channel");
			}
		}

		LOG.info("End of heartbeats");

		started = false;

		guicePluginManager.shutdown();

		LOG.info("Stop called: stopped, waiting for processes to finish, " + timeoutSeconds + " seconds max");

		waitForStop();

		LOG.info("MMServer finished");
	}

	private void waitForStop() {
		final CountDownLatch cdl = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			while (isRunning()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOG.error("Stop waiter interrupted", e);
				}
			}
			cdl.countDown();
		});
		thread.setDaemon(false);
		thread.start();

		try {
			if (cdl.await(timeoutSeconds, TimeUnit.SECONDS)) {
				LOG.info("Stop called: stopped in time");
			} else {
				LOG.info("Stop called: timeout");
				LOG.info(runningSet().toString());
			}
		} catch (InterruptedException e) {
			LOG.error("Stop called", e);
		}
	}

	/**
	 * Returns true if there is at least one agent which is running
	 */
	@Override
	public final boolean isRunning() {
		Collection<ChannelSlot> channels = ProcessEngine.getInstance().getChannels();
		for (ChannelSlot channelSlot : channels) {
			PluggableChannel pluggedChannel = channelSlot.getPluggedChannel();
			if (pluggedChannel instanceof ProcessingChannel) {
				int nb = pluggedChannel.getNbWorking() + pluggedChannel.getNbWaiting();
				if (nb > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a set of agents which are running at that moment
	 */
	@Override
	public final Set<String> runningSet() {
		Set<String> set = new HashSet<>();
		Collection<ChannelSlot> channels = ProcessEngine.getInstance().getChannels();
		for (ChannelSlot channelSlot : channels) {
			PluggableChannel pluggedChannel = channelSlot.getPluggedChannel();
			if (pluggedChannel instanceof ProcessingChannel) {
				int nb = pluggedChannel.getNbWorking() + pluggedChannel.getNbWaiting();
				if (nb > 0) {
					set.add(pluggedChannel.getName());
				}
			}
		}
		return set;
	}
}
