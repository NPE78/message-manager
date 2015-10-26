package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import com.google.inject.Singleton;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 22/10/2015.
 */
class DefaultTestMMModule extends AbstractSynaptixIntegratorServletModule {

	@Override
	protected final void configure() {
		bind(IProcessErrorFactory.class).to(DefaultProcessErrorFactory.class).in(Singleton.class);
	}

	protected void configureTestModule() {
	}
}
