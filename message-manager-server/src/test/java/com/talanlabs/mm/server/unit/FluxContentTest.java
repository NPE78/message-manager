package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.engine.implem.DefaultMessage;
import com.talanlabs.mm.server.delegate.FluxContentManager;
import com.talanlabs.mm.server.exception.ContentNotFetchedException;
import com.talanlabs.mm.server.exception.ContentNotSavedException;
import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.shared.model.IFSMessage;
import java.util.UUID;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 04/04/2016.
 */
public class FluxContentTest {

	@Test
	public void testGetContentException() throws Exception {
		DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
		manager.setBaseFile((FileObject) null);

		FluxContentManager delegate = new FluxContentManager();

		boolean exceptionRaised = false;
		try {
			delegate.getTestFluxContent(UUID.randomUUID().toString());
		} catch (ContentNotFetchedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);

		exceptionRaised = false;
		try {
			delegate.getContent(new DefaultMessage("MT1"));
		} catch (ContentNotFetchedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	@Test
	public void testSetContentException() throws Exception {
		DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
		manager.setBaseFile((FileObject) null);

		FluxContentManager delegate = new FluxContentManager();

		boolean exceptionRaised = false;
		try {
			delegate.setTestFluxContent("test", UUID.randomUUID().toString());
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);

		exceptionRaised = false;
		try {
			delegate.setContent("test", new DefaultMessage("MT1"));
		} catch (ContentNotSavedException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	@Test
	public void testGetSetTestFluxContent() throws Exception {
		FSHelper.fixBaseDir();

		FluxContentManager delegate = new FluxContentManager();

		String id = UUID.randomUUID().toString();
		String text = "test " + id;

		delegate.setTestFluxContent(text, id);
		Assert.assertEquals(text, delegate.getTestFluxContent(id));
	}

	@Test
	public void testGetSetContent() throws Exception {
		FSHelper.fixBaseDir();

		FluxContentManager delegate = new FluxContentManager();

		String id = UUID.randomUUID().toString();
		String text = "test " + id;

		IFSMessage message = new DefaultMessage("message", id);
		delegate.setContent(text, message);
		Assert.assertEquals(text, delegate.getContent(message));
	}
}
