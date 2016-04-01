package com.synaptix.mm.server.it;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Inject;
import com.synaptix.entity.IId;
import com.synaptix.entity.IdRaw;
import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.server.delegate.FluxContentServiceDelegate;
import com.synaptix.mm.server.exception.ContentNotFetchedException;
import com.synaptix.mm.server.exception.ContentNotSavedException;
import com.synaptix.mm.shared.model.IFSMessage;

/**
 * Created by NicolasP on 06/01/2016.
 */
public class ITMessageContent extends AbstractMMTest {

	@Inject
	private FluxContentServiceDelegate fluxContentServiceDelegate;

	@Test
	public void testNew() throws Exception {
		IFSMessage message = new DefaultMessage("test");

		message.setFolder(".." + File.separator + "messageTest");

		boolean exceptionRaised = false;
		try {
			System.out.println("Testing get content for 1st case");
			Assert.assertNull(fluxContentServiceDelegate.getContent(message));
		} catch (ContentNotFetchedException e) {
			exceptionRaised = e.getCause() instanceof FileNotFoundException;
		}
		Assert.assertTrue(exceptionRaised);

		System.out.println("Testing set content for 1st case");
		fluxContentServiceDelegate.setContent("test", message);

		System.out.println("Testing get content after set content for 1st case");
		Assert.assertEquals("test", fluxContentServiceDelegate.getContent(message));

		System.out.println("Testing set content 2 for 1st case");
		fluxContentServiceDelegate.setContent("test plus complet", message);

		System.out.println("Testing get content after set content 2 for 1st case");
		Assert.assertEquals("test plus complet", fluxContentServiceDelegate.getContent(message));
	}

	@Test
	public void testArchive() throws Exception {

		File file = new File("./src/test/flux/contentTest.tgz");
		Assert.assertTrue(file.exists());

		FileSystemManager manager = VFS.getManager();
		FileObject fileObject = manager.resolveFile(file.getAbsolutePath());
		FileObject destObject = manager.resolveFile("./contentTest.tgz");
		destObject.copyFrom(fileObject, Selectors.SELECT_SELF);

		IFSMessage message = new DefaultMessage("test", "87e8ded7-b3a9-486c-a947-95bd1e6e61d0");

		message.setFolder(".." + File.separator + "contentTest");

		System.out.println("Testing get content for 2nd case");
		Assert.assertEquals("OK", fluxContentServiceDelegate.getContent(message));

		boolean exceptionRaised = false;
		try {
			System.out.println("Testing set content for 2nd case");
			fluxContentServiceDelegate.setContent("test", message);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	@Test
	public void testFlux() throws Exception {
		IFSMessage message = new DefaultMessage(null);
		IId id = new IdRaw(message.getId().toString());

		Assert.assertNull(fluxContentServiceDelegate.getTestFluxContent(id));

		String text = "test";
		fluxContentServiceDelegate.setTestFluxContent(text, id);
		Assert.assertEquals(text, fluxContentServiceDelegate.getTestFluxContent(id));

		text = "test2";
		fluxContentServiceDelegate.setTestFluxContent(text, id);
		Assert.assertEquals(text, fluxContentServiceDelegate.getTestFluxContent(id));

		text = null;
		fluxContentServiceDelegate.setTestFluxContent(text, id);
		Assert.assertNull(fluxContentServiceDelegate.getTestFluxContent(id));
	}

	@Test
	public void exceptionMessage() throws Exception {
		IFSMessage message = new DefaultMessage(null);
		IId id = new IdRaw(message.getId().toString());

		boolean exceptionRaised = false;
		try {
			fluxContentServiceDelegate.setContent("test", null);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);

		exceptionRaised = false;
		try {
			fluxContentServiceDelegate.setContent("test", message);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);

		message.setFolder("contentTest");

		message = new DefaultMessage("");
		exceptionRaised = false;
		try {
			fluxContentServiceDelegate.setContent("test", message);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
			e.printStackTrace();
		}
		Assert.assertTrue(exceptionRaised);
	}
}
