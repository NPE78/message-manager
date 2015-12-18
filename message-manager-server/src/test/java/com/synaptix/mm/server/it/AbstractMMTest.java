package com.synaptix.mm.server.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Injector;
import com.synaptix.mm.server.IServer;
import com.synaptix.mm.server.MMServer;
import com.synaptix.mm.server.implem.DefaultTestMMServerModule;
import com.synaptix.mm.server.unit.MainIntegratorBoot;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class AbstractMMTest {

	protected static final Log LOG = LogFactory.getLog(AbstractMMTest.class);

	private Injector injector;

	private boolean started;

	@Before
	public void init() {
		createInjector();
		injector.injectMembers(this);

		if (autoStartIntegrator()) {
			startIntegrator();
		}
	}

	/**
	 * Start the integrator
	 */
	protected final void startIntegrator() {
		getServer().start();
		this.started = true;
	}

	public boolean autoStartIntegrator() {
		return true;
	}

	protected void createInjector() {
		injector = MainIntegratorBoot.createInjector(buildIntegratorTestModule());
	}

	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new DefaultTestMMServerModule();
	}

	protected Class<? extends IServer> getServerClass() {
		return MMServer.class;
	}

	/**
	 * Start the integrator by using {@link #startIntegrator()}
	 */
	protected final IServer getServer() {
		if (injector == null) {
			return null;
		}
		return injector.getInstance(getServerClass());
	}

	protected final <I> I getInstance(Class<I> clazz) {
		return injector.getInstance(clazz);
	}

	@After
	public void stopIntegrator() {
		if (started) {
			IServer server = getServer();
			if (server != null) {
				server.stop();
			}
		}
	}

	protected final void waitIntegrator() {
		waitIntegrator(10);
	}

	protected final void waitIntegrator(int timeout) {
		IServer server = getServer();

		final CountDownLatch cdl = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			while (server.isRunning()) {
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
			if (cdl.await(timeout, TimeUnit.SECONDS)) {
				LOG.info("Stop called: waited in time");
			} else {
				LOG.info("Stop called: timeout");
				LOG.info(server.runningSet().toString());
			}
		} catch (InterruptedException e) {
			LOG.error("Stop called", e);
		}
	}

	public String readFile(String filename) {
		StringBuilder content = new StringBuilder();
		try {
			Path file = Paths.get(filename);
			BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (content.length() > 0) {
					content.append("/n");
				}
				content.append(line);
			}
		} catch (IOException e) {
			LOG.error("IOException", e);
		}
		return content.toString();
	}
}
