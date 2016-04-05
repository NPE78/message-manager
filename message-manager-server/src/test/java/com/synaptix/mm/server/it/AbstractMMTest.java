package com.synaptix.mm.server.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
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
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.server.helper.ServerHelper;
import com.synaptix.mm.server.unit.MainIntegratorBoot;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class AbstractMMTest {

	protected static final Log LOG = LogFactory.getLog(AbstractMMTest.class);

	private Injector injector;

	private boolean started;

	private IServer server;

	@Before
	public void init() {
		ServerHelper.configureServer();

		createInjector();

		injector.injectMembers(this);

		if (autoStartIntegrator()) {
			startIntegrator();
		}
	}

	protected String getIntegFolder() {
		return FSHelper.getIntegFolder();
	}

	/**
	 * Start the integrator
	 */
	protected final void startIntegrator() {
		server.start(getIntegFolder());
		this.started = true;
	}

	public boolean autoStartIntegrator() {
		return true;
	}

	protected void createInjector() {
		injector = MainIntegratorBoot.createInjector(getPropertyUrl(), buildIntegratorTestModule());
		server = injector.getInstance(getServerClass());
	}

	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return null;
	}

	protected Class<? extends IServer> getServerClass() {
		return MMServer.class;
	}

	/**
	 * Start the integrator by using {@link #startIntegrator()}
	 */
	protected final IServer getServer() {
		return server;
	}

	protected final <I> I getInstance(Class<I> clazz) {
		return injector.getInstance(clazz);
	}

	@After
	public void stopIntegrator() {
		if (started) {
			if (server != null) {
				server.stop();
			}
		}
	}

	protected final void waitIntegrator() {
		waitIntegrator(10);
	}

	protected final void waitIntegrator(int timeout) {
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

	protected URL getPropertyUrl() {
		return this.getClass().getClassLoader().getResource("integrator.properties");
	}
}
