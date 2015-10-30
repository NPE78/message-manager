package com.synaptix.mm.engine.it;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultTestMMModule extends AbstractModule {

	@Override
	protected final void configure() {
		bind(IProcessErrorFactory.class).to(DefaultProcessErrorFactory.class).in(Singleton.class);

		configureTestModule();
	}

	protected void configureTestModule() {
	}
}
