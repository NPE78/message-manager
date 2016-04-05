package com.synaptix.mm.server.it;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.server.implem.DefaultMMInjector;
import com.synaptix.mm.shared.model.IFSMessage;

/**
 * Created by NicolasP on 01/04/2016.
 */
public class ITInjector extends AbstractMMTest {

	@Test
	public void injectTest() throws Exception {

		String filename = "test";

		DefaultMMInjector injector = getInstance(DefaultMMInjector.class);
		FileObject file = VFS.getManager().resolveFile(injector.getWorkDir().getAbsolutePath());
		FileObject lock = file.resolveFile(filename + ".lck");
		lock.createFile();

		FileObject messageFile = file.resolveFile(filename);
		try (OutputStream out = messageFile.getContent().getOutputStream()) {
			out.write("test".getBytes(Charset.forName("UTF-8")));
		}

		lock.delete();

		new CountDownLatch(1).await(2, TimeUnit.SECONDS);

		waitIntegrator(120);

		messageFile.refresh();
		Assert.assertFalse(messageFile.exists());

		IFSMessage message = injector.getLastMessage();
		Assert.assertEquals("accepted", message.getFolder());
		Assert.assertNotNull(message.getFirstProcessingDate());
		Assert.assertNotNull(message.getId());
		Assert.assertEquals(message.getFile().getName(), message.getId().toString());
	}

	@Test
	public void injectTestDeadline() throws Exception {

		DefaultMMInjector injector = getInstance(DefaultMMInjector.class);

		DefaultMessage message = new DefaultMessage("MT1");
		DefaultMessageType messageType = (DefaultMessageType) message.getMessageType();
		messageType.setRecyclingDeadline(5);
		message.setFirstProcessingDate(Instant.now().minus(10, ChronoUnit.MINUTES));
		injector.injectLiseaMessage(message);

		Assert.assertNotNull(message.getFirstProcessingDate());
		Assert.assertNotNull(message.getId());
	}
}
