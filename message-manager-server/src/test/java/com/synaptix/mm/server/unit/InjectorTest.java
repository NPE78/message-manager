package com.synaptix.mm.server.unit;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.server.implem.DefaultMMInjector;
import com.synaptix.mm.shared.model.IFSMessage;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.tmgr.libs.tasks.filesys.FolderEventTriggerTask;

/**
 * Created by NicolasP on 05/04/2016.
 */
public class InjectorTest {

	@Test
	public void testInjector() throws Exception {
		FSHelper.fixBaseDir();

		DefaultMMInjector mmInjector = new DefaultMMInjector();

		Injector guiceInjector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				Map<String, IMessageType> messageTypeMap = new HashMap<>();
				messageTypeMap.put("DEFAULT", new DefaultMessageType("DEFAULT"));
				bind(new TypeLiteral<Map<String, IMessageType>>() {
				}).annotatedWith(Names.named("messageTypeMap")).toInstance(messageTypeMap);
			}
		});
		guiceInjector.injectMembers(mmInjector);

		File file = File.createTempFile("test_injector", "txt");
		try (FileWriter writer = new FileWriter(file)) {
			writer.write("test mmInjector");
		}
		File injectorPath = new File(file.getParentFile().getAbsolutePath() + "/injectorTest" + UUID.randomUUID().toString() + "/");
		injectorPath.mkdirs();

		File f = new File(injectorPath.getAbsolutePath() + "/" + file.getName());
		file.renameTo(f);
		file = f;

		mmInjector.setWorkDir(injectorPath);
		new File(injectorPath.getAbsolutePath() + "/accepted/").mkdirs();
		new File(injectorPath.getAbsolutePath() + "/rejected/").mkdirs();

		mmInjector.inject(new FolderEventTriggerTask.NewFileTriggerEvent(file, null));

		IFSMessage lastMessage = mmInjector.getLastMessage();
		Assert.assertEquals("accepted", lastMessage.getFolder());
		Assert.assertEquals("DEFAULT", lastMessage.getMessageType().getName());
		Assert.assertNotNull(lastMessage.getFirstProcessingDate());
		Assert.assertNotNull(lastMessage.getDeadlineDate());
	}
}
