package com.synaptix.mm.server.unit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.synaptix.mm.engine.it.DefaultIntegConfig;
import com.synaptix.mm.server.agent.BatchArchiveAgent;
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.server.implem.DefaultMMInjector;
import com.synaptix.mm.shared.model.IIntegConfig;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.pmgr.trigger.injector.IInjector;

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

		Set<Class<? extends IInjector>> injectorSet = new HashSet<>();
		injectorSet.add(DefaultMMInjector.class);

		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(IIntegConfig.class).toInstance(new DefaultIntegConfig());
				bind(new TypeLiteral<Set<Class<? extends IInjector>>>() {
					 }
				).toInstance(injectorSet);
				bind(new TypeLiteral<Map<String, IMessageType>>() {
				}).annotatedWith(Names.named("messageTypeMap")).toInstance(new HashMap<>());
			}
		});

		DefaultMMInjector mmInjector = new DefaultMMInjector();

		injector.injectMembers(agent);
		injector.injectMembers(mmInjector);

		Assert.assertEquals(fileDestination.getURL(), agent.getScript(script));

		// test with files
		File file = createFile();

		File archivePath = new File(file.getParentFile().getAbsolutePath() + "/archiveTest" + UUID.randomUUID().toString() + "/");
		archivePath.mkdirs();
		mmInjector.setWorkDir(archivePath);
		file.renameTo(new File(archivePath.getAbsolutePath() + "/" + file.getName()));

		agent.work(archivePath.getAbsolutePath(), null);
		if (SystemUtils.IS_OS_LINUX) {
			System.out.println(file);
			Assert.assertFalse(file.exists());
		}

		file = createFile();
		file.renameTo(new File(archivePath.getAbsolutePath() + "/" + file.getName()));
		agent.work(null, null);
		if (SystemUtils.IS_OS_LINUX) {
			System.out.println(file);
			Assert.assertFalse(file.exists());
		}
	}

	private File createFile() throws IOException {
		File file = File.createTempFile("test_archive", "txt");
		Calendar calendar = Calendar.getInstance();
		calendar.roll(4, Calendar.DAY_OF_WEEK);
		file.setLastModified(calendar.getTimeInMillis());
		return file;
	}
}
