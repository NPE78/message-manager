package com.synaptix.mm.server.unit;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

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

		DefaultMMInjector injector = new DefaultMMInjector();

		Injector guiceInjector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				Map<String, IMessageType> messageTypeMap = new HashMap<>();
				messageTypeMap.put("DEFAULT", new DefaultMessageType("DEFAULT"));
				bind(new TypeLiteral<Map<String, IMessageType>>() {
				}).annotatedWith(Names.named("messageTypeMap")).toInstance(messageTypeMap);
			}
		});
		guiceInjector.injectMembers(injector);

		File file = File.createTempFile("test_injector", "txt");
		try (FileWriter writer = new FileWriter(file)) {
			writer.write("test injector");
		}
		injector.setWorkDir(file.getParentFile());
		new File(file.getParentFile().getAbsolutePath() + "/accepted/").mkdirs();
		new File(file.getParentFile().getAbsolutePath() + "/rejected/").mkdirs();

		injector.inject(new FolderEventTriggerTask.NewFileTriggerEvent(file, null));

		IFSMessage lastMessage = injector.getLastMessage();
		Assert.assertEquals("accepted", lastMessage.getFolder());
		Assert.assertEquals("DEFAULT", lastMessage.getMessageType().getName());
		Assert.assertNotNull(lastMessage.getFirstProcessingDate());
		Assert.assertNotNull(lastMessage.getDeadlineDate());
	}
}
