package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import com.google.inject.Singleton;
import com.synaptix.mm.engine.factory.IErrorFactory;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 22/10/2015.
 */
public abstract class DefaultTestMMModule extends AbstractSynaptixIntegratorServletModule {

	@Override
	protected final void configure() {
		bind(IErrorFactory.class).to(SimpleErrorFactory.class).in(Singleton.class);
	}

	protected void configureTestModule() {

	}
}
