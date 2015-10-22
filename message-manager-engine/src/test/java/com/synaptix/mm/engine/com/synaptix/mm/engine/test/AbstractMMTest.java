package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Injector;
import com.synaptix.mm.engine.MMServer;
import com.synaptix.mm.engine.MainIntegratorBoot;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class AbstractMMTest {

	private Injector injector;

	@Before
	public void startIntegrator() {
		injector = MainIntegratorBoot.createServer(buildIntegratorTestModule());
	}

	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return null;
	}

	protected final MMServer getServer() {
		return injector.getInstance(MMServer.class);
	}

	public <I> I getInstance(Class<I> clazz) {
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
