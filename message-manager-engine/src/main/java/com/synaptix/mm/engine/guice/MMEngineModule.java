package com.synaptix.mm.engine.guice;

import com.google.inject.*;
import com.google.inject.name.*;
import com.synaptix.mm.engine.*;
import com.synaptix.pmgr.guice.*;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMEngineModule extends AbstractModule {

	private final String trmt;

	public MMEngineModule(String trmt) {
		super();

		this.trmt = trmt;
	}

	@Override
	protected void configure() {
		install(new ProcessManagerModule());

		bind(String.class).annotatedWith(Names.named("trmt")).toInstance(trmt);

		bind(MMServer.class).in(Singleton.class);
	}
}
