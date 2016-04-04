package com.synaptix.mm.server.unit;

import java.io.File;
import java.util.UUID;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

import com.synaptix.entity.IId;
import com.synaptix.entity.IdRaw;
import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.server.delegate.FluxContentServiceDelegate;
import com.synaptix.mm.server.exception.ContentNotFetchedException;
import com.synaptix.mm.server.exception.ContentNotSavedException;
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.shared.model.IFSMessage;

/**
 * Created by NicolasP on 04/04/2016.
 */
public class FluxContentTest {

	@Test
	public void testGetContentException() {
		FluxContentServiceDelegate delegate = new FluxContentServiceDelegate();

		boolean exceptionRaised = false;
		try {
			delegate.getTestFluxContent(new IdRaw(UUID.randomUUID().toString()));
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
	public void testSetContentException() {
		FluxContentServiceDelegate delegate = new FluxContentServiceDelegate();

		boolean exceptionRaised = false;
		try {
			delegate.setTestFluxContent("test", new IdRaw(UUID.randomUUID().toString()));
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
		try {
			DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
			if (manager.getBaseFile() == null) {
				manager.setBaseFile(new File(FSHelper.getIntegFolder()));
				if (!manager.getBaseFile().exists()) {
					manager.getBaseFile().createFolder();
				}
			}
		} catch (FileSystemException e) {
			Assert.fail();
		}

		FluxContentServiceDelegate delegate = new FluxContentServiceDelegate();

		IId id = new IdRaw(UUID.randomUUID().toString());
		String text = "test " + id;

		delegate.setTestFluxContent(text, id);
		Assert.assertEquals(text, delegate.getTestFluxContent(id));
	}

	@Test
	public void testGetSetContent() throws Exception {
		try {
			DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
			if (manager.getBaseFile() == null) {
				manager.setBaseFile(new File(FSHelper.getIntegFolder()));
				if (!manager.getBaseFile().exists()) {
					manager.getBaseFile().createFolder();
				}
			}
		} catch (FileSystemException e) {
			Assert.fail();
		}

		FluxContentServiceDelegate delegate = new FluxContentServiceDelegate();

		IId id = new IdRaw(UUID.randomUUID().toString());
		String text = "test " + id;

		IFSMessage message = new DefaultMessage("message", id);
		delegate.setContent(text, message);
		Assert.assertEquals(text, delegate.getContent(message));
	}
}
