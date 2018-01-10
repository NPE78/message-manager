//package com.talanlabs.mm.server.unit;
//
//import com.talanlabs.mm.server.implem.DefaultMMInjector;
//import java.io.File;
//import java.io.FileWriter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import com.google.inject.AbstractModule;
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//import com.google.inject.TypeLiteral;
//import com.google.inject.name.Names;
//import com.talanlabs.mm.engine.model.DefaultMessageType;
//import com.talanlabs.mm.server.helper.FSHelper;
//import com.talanlabs.mm.shared.model.IFSMessage;
//import com.talanlabs.mm.shared.model.IMessageType;
//import com.synaptix.tmgr.libs.tasks.filesys.FolderEventTriggerTask;
//
///**
// * Created by NicolasP on 05/04/2016.
// */
//public class InjectorTest {
//
//	@Test
//	public void testInjector() throws Exception {
//		FSHelper.fixBaseDir();
//
//		DefaultMMInjector mmInjector = new DefaultMMInjector();
//
//
//		File file = File.createTempFile("test_injector", "txt");
//		try (FileWriter writer = new FileWriter(file)) {
//			writer.write("test mmInjector");
//		}
//		File injectorPath = new File(file.getParentFile().getAbsolutePath() + "/injectorTest" + UUID.randomUUID().toString() + "/");
//		injectorPath.mkdirs();
//
//		File f = new File(injectorPath.getAbsolutePath() + "/" + file.getName());
//		file.renameTo(f);
//		file = f;
//
//		mmInjector.setWorkDir(injectorPath);
//		new File(injectorPath.getAbsolutePath() + "/accepted/").mkdirs();
//		new File(injectorPath.getAbsolutePath() + "/rejected/").mkdirs();
//
//		mmInjector.inject(new FolderEventTriggerTask.NewFileTriggerEvent(file, null));
//
//		IFSMessage lastMessage = mmInjector.getLastMessage();
//		Assert.assertEquals("accepted", lastMessage.getFolder());
//		Assert.assertEquals("DEFAULT", lastMessage.getMessageType().getName());
//		Assert.assertNotNull(lastMessage.getFirstProcessingDate());
//		Assert.assertNotNull(lastMessage.getDeadlineDate());
//	}
//}
