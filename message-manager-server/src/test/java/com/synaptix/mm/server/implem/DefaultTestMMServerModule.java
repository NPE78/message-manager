package com.synaptix.mm.server.implem;

import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.mm.engine.it.DefaultTestMMModule;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 30/10/2015.
 */
public class DefaultTestMMServerModule extends AbstractSynaptixIntegratorServletModule {

	private final Class<? extends IProcessErrorFactory> processErrorFactoryClass;

	public DefaultTestMMServerModule() {
		this(DefaultProcessErrorFactory.class);
	}

	public DefaultTestMMServerModule(Class<? extends IProcessErrorFactory> processErrorFactoryClass) {
		this.processErrorFactoryClass = processErrorFactoryClass;
	}

	@Override
	protected final void configure() {
		install(new DefaultTestMMModule(processErrorFactoryClass));

		bindAgent(DefaultMMAgent.class, 10, 10);
		bindInjector(DefaultMMInjector.class);

		configureTestModule();
	}

	protected void configureTestModule() {
	}
}
