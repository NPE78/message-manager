package com.synaptix.mm.server.unit;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.synaptix.mm.engine.it.DefaultIntegConfig;
import com.synaptix.mm.server.agent.BatchArchiveAgent;
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.shared.model.IIntegConfig;

/**
 * Created by NicolasP on 04/04/2016.
 */
public class ArchiveAgentTest {

	@Test
	public void testArchiveAgent() {
		FSHelper.fixBaseDir();

		BatchArchiveAgent agent = new BatchArchiveAgent();

		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(IIntegConfig.class).toInstance(new DefaultIntegConfig());
			}
		});

		injector.injectMembers(agent);

		URL script = getClass().getClassLoader().getResource("dirarch");
		Assert.assertEquals(script, agent.getScript(script));

		agent.work("./flux/message", null);
	}
}
