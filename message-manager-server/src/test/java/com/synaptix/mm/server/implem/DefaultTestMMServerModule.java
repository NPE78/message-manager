package com.synaptix.mm.server.implem;

import com.synaptix.mm.engine.it.DefaultTestMMModule;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 30/10/2015.
 */
public class DefaultTestMMServerModule extends AbstractSynaptixIntegratorServletModule {

	@Override
	protected final void configure() {
		install(new DefaultTestMMModule());

		configureTestModule();
	}

	protected void configureTestModule() {
	}
}
