package com.synaptix.mm.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.synaptix.mm.engine.guice.MMEngineModule;
import com.synaptix.mm.server.MMServer;
import com.synaptix.pmgr.guice.ProcessManagerModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMServerModule extends AbstractModule {

	private final String trmt;

	public MMServerModule(String trmt) {
		super();

		this.trmt = trmt;
	}

	@Override
	protected void configure() {
		install(new MMEngineModule());
		install(new ProcessManagerModule());

		bind(String.class).annotatedWith(Names.named("trmt")).toInstance(trmt);
		bind(MMServer.class).in(Singleton.class);
	}
}
