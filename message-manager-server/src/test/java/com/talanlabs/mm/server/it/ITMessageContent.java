package com.talanlabs.mm.server.it;

import com.talanlabs.mm.engine.implem.DefaultMessage;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.delegate.FluxContentServiceDelegate;
import com.talanlabs.mm.server.exception.ContentNotFetchedException;
import com.talanlabs.mm.server.exception.ContentNotSavedException;
import com.talanlabs.mm.shared.model.IFSMessage;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by NicolasP on 06/01/2016.
 */
public class ITMessageContent extends AbstractMMTest {

	private FluxContentServiceDelegate fluxContentServiceDelegate;

	@Before
    public void before() {
        fluxContentServiceDelegate = new FluxContentServiceDelegate();
    }

    @Override
    public boolean autoStartIntegrator() {
        return true;
    }

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

		File file = new File("./src/test/flux/message/contentTest.tgz");
		Assert.assertTrue(file.exists());

		FileSystemManager manager = VFS.getManager();
		FileObject fileObject = manager.resolveFile(file.getAbsolutePath());
		FileObject destObject = manager.resolveFile("./message/contentTest.tgz");
		destObject.copyFrom(fileObject, Selectors.SELECT_SELF);

		Assertions.assertThat(destObject.exists()).isTrue();

		IFSMessage message = new DefaultMessage("message", "87e8ded7-b3a9-486c-a947-95bd1e6e61d0");
        message.setFolder("contentTest");

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
		String id = message.getId().toString();

        FileObject testFlux = VFS.getManager().getBaseFile().resolveFile("testFlux");
        if (!testFlux.exists()) {
            testFlux.createFolder();
        }

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
	public void exceptionMessage() {

		boolean exceptionRaised = false;
		try {
			fluxContentServiceDelegate.setContent("test", null);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);

		exceptionRaised = false;
		try {
			IFSMessage message = new DefaultMessage(null);
			fluxContentServiceDelegate.setContent("test", message);
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}
}
