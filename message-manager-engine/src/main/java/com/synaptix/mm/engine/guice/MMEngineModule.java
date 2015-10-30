package com.synaptix.mm.engine.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.MMEngine;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMEngineModule extends AbstractModule {

	public MMEngineModule() {
		super();
	}

	@Override
	protected void configure() {
		bind(MMDictionary.class).in(Singleton.class);
		bind(MMEngine.class).in(Singleton.class);
	}
}
