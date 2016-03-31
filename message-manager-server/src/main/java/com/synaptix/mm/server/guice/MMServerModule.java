package com.synaptix.mm.server.guice;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.synaptix.mm.engine.guice.MMEngineModule;
import com.synaptix.mm.server.MMServer;
import com.synaptix.mm.server.delegate.FluxContentServiceDelegate;
import com.synaptix.mm.shared.model.IMessageType;
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

		bind(new TypeLiteral<Map<String, IMessageType>>() {
		}).annotatedWith(Names.named("messageTypeMap")).toInstance(new HashMap<>());

		bind(FluxContentServiceDelegate.class).in(Singleton.class);
	}
}
