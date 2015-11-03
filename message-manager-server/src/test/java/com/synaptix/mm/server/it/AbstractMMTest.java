package com.synaptix.mm.server.it;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Injector;
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

	@Before
	public void startIntegrator() {
		injector = MainIntegratorBoot.createServer(buildIntegratorTestModule());
	}

	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new DefaultTestMMServerModule();
	}

	protected final MMServer getServer() {
		return injector.getInstance(MMServer.class);
	}

	protected final <I> I getInstance(Class<I> clazz) {
		return injector.getInstance(clazz);
	}

	@After
	public void stopIntegrator() {
		MMServer server = getServer();
		if (server != null) {
			server.stop();
		}
	}

	protected final void waitIntegrator() {
		waitIntegrator(10);
	}

	protected final void waitIntegrator(int timeout) {
		MMServer server = getServer();

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
				LOG.info("Stop called: stopped in time");
			} else {
				LOG.info("Stop called: timeout");
				LOG.info(server.runningSet().toString());
			}
		} catch (InterruptedException e) {
			LOG.error("Stop called", e);
		}
	}
}
