package com.synaptix.mm.engine;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.logging.*;
import org.joda.time.*;

import com.google.inject.*;
import com.google.inject.name.*;
import com.synaptix.component.factory.*;
import com.synaptix.entity.extension.*;
import com.synaptix.pmgr.core.apis.*;
import com.synaptix.pmgr.core.lib.*;
import com.synaptix.pmgr.core.lib.ProcessingChannel.*;
import com.synaptix.pmgr.core.lib.probe.*;
import com.synaptix.pmgr.plugin.*;
import com.synaptix.toolkits.properties.*;

public class MMServer {

	private static final Log LOG = LogFactory.getLog(MMServer.class);

	private final String trmt;

	private final Injector injector;

	private boolean started;

	@Inject
	public MMServer(Injector injector, @Named("trmt") String trmt) {
		super();

		this.injector = injector;
		this.trmt = trmt;

		Locale.setDefault(Locale.FRANCE);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		DateTimeZone.setDefault(DateTimeZone.forID("UTC"));

		ComponentFactory.getInstance().addExtension(IDatabaseComponentExtension.class, new DatabaseComponentExtensionProcessor());
		ComponentFactory.getInstance().addExtension(IBusinessComponentExtension.class, new BusinessComponentExtensionProcessor());
		ComponentFactory.getInstance().addExtension(ICacheComponentExtension.class, new CacheComponentExtensionProcessor());
		ComponentFactory.getInstance().setComputedFactory(new DefaultComputedFactory());
	}

	private String getVersion() {
		String version = getClass().getPackage().getImplementationVersion();
		if (version == null) {
			version = "local";
		}
		// try {
		// Properties p = new Properties();
		// p.load(PscXmppServer.class.getResourceAsStream("/version.properties"));
		// res = p.getProperty("version", res);
		// } catch (IOException e) {
		// LOG.error(e.getMessage(), e);
		// }
		return version;
	}

	public void start() {
		LOG.info("START MMServer");

		String version = getVersion();
		LOG.info("Version : " + version);

		launchEngine();
	}

	public final void launchEngine() {
		Properties properties = PropertiesKit.load("process_engine.properties", ProcessEngine.class, true);
//		if (config != null) {
//			String ip = integratorConfig.getEngineAddress();
//			if ((ip != null) && (!ip.isEmpty())) {
//				properties.setProperty("engine" + trmt + ".bindaddress", ip);
//			}
//		}
		GuicePluginManager.initProcessManager(LOG, trmt, properties);

		this.started = true;
	}

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

		ProcessEngine.shutdown();

		LOG.info("Stop called: stopped, waiting for processes to finish");

		final CountDownLatch cdl = new CountDownLatch(1);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isRunning()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				cdl.countDown();
			}
		});
		thread.setDaemon(false);
		thread.start();

		try {
			if (cdl.await(2, TimeUnit.MINUTES)) {
				LOG.info("Stop called: stopped in time");
			} else {
				LOG.info("Stop called: timeout");
				LOG.info(runningSet().toString());
			}
		} catch (InterruptedException e) {
			LOG.error("Stop called", e);
		}

		LOG.info("MMServer finished");
	}

	public boolean isRunning() {
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

	private Set<String> runningSet() {
		Set<String> set = new HashSet<String>();
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
