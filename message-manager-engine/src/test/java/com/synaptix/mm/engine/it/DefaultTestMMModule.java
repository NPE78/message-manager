package com.synaptix.mm.engine.it;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultTestMMModule extends AbstractModule {

	private final Class<? extends IProcessErrorFactory> processErrorFactoryClass;

	public DefaultTestMMModule(Class<? extends IProcessErrorFactory> processErrorFactoryClass) {
		this.processErrorFactoryClass = processErrorFactoryClass;
	}

	@Override
	protected final void configure() {
		bind(IProcessErrorFactory.class).to(processErrorFactoryClass).in(Singleton.class);

		configureTestModule();
	}

	protected void configureTestModule() {
	}
}
