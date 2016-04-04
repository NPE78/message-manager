package com.synaptix.mm.server.unit;

import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
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
	public void testArchiveAgent() throws Exception {
		FSHelper.fixBaseDir();

		BatchArchiveAgent agent = new BatchArchiveAgent();

		URL script = getClass().getClassLoader().getResource("dirarch");
		agent.getScript(script); // create of update script

		FileObject fileDestination = VFS.getManager().getBaseFile().resolveFile("archive");
		if (fileDestination.exists()) {
			fileDestination.delete();
		}

		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(IIntegConfig.class).toInstance(new DefaultIntegConfig());
			}
		});

		injector.injectMembers(agent);

		Assert.assertEquals(fileDestination.getURL(), agent.getScript(script));

		agent.work("./flux/message", null);
	}
}
