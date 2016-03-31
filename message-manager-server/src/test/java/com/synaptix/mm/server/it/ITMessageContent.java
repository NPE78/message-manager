package com.synaptix.mm.server.it;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Inject;
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

	/**
	 * The test needs to be played from the project root, that's why there is a main here
	 */
	public static void main(String[] args) {
		ITMessageContent mainFluxContent = new ITMessageContent();

		mainFluxContent.init();

		try {
			mainFluxContent.testNew();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			mainFluxContent.testArchive();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		mainFluxContent.waitIntegrator();

		mainFluxContent.stopIntegrator();
	}

	@Test
	public void testNew() {
		try {
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

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testArchive() {

		File file = new File("./src/test/flux/contentTest.tgz");
		Assert.assertTrue(file.exists());

		try {
			FileSystemManager manager = VFS.getManager();
			FileObject fileObject = manager.resolveFile(file.getAbsolutePath());
			FileObject destObject = manager.resolveFile("./contentTest.tgz");
			destObject.copyFrom(fileObject, Selectors.SELECT_SELF);
		} catch (FileSystemException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		try {
			IFSMessage message =  new DefaultMessage("test", "87e8ded7-b3a9-486c-a947-95bd1e6e61d0");

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

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
