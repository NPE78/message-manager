package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Injector;
import com.synaptix.mm.engine.MMServer;
import com.synaptix.mm.engine.MainIntegratorBoot;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
class AbstractMMTest {

	protected static final Log LOG = LogFactory.getLog(AbstractMMTest.class);

	private Injector injector;

	@Before
	public void startIntegrator() {
		injector = MainIntegratorBoot.createServer(buildIntegratorTestModule());
	}

	AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new DefaultTestMMModule();
	}

	final MMServer getServer() {
		return injector.getInstance(MMServer.class);
	}

	<I> I getInstance(Class<I> clazz) {
		return injector.getInstance(clazz);
	}

	@After
	public void stopIntegrator() {
		MMServer server = getServer();
		if (server != null) {
			server.stop();
		}
	}
}
