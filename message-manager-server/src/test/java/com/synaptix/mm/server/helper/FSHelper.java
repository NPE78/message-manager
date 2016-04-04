package com.synaptix.mm.server.helper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Assert;

/**
 * Created by NicolasP on 31/03/2016.
 */
public class FSHelper {

	public static String getIntegFolder() {
		URL location = FSHelper.class.getProtectionDomain().getCodeSource().getLocation();
		String path = location.getPath();
		String messageManagerServer = File.separator + "message-manager-server" + File.separator; //$NON-NLS-1$
		int messageManagerServerIdx = path.lastIndexOf("/message-manager-server/");
		File file;
		if (messageManagerServerIdx < 0) {
			try {
				file = new File(new File(location.toURI()).getParentFile() + "flux");
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		} else {
			file = new File(path.substring(0, messageManagerServerIdx) + messageManagerServer + "flux");
		}
		return file.getAbsolutePath();
	}

	public static void fixBaseDir() {
		try {
			DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
			if (manager.getBaseFile() == null) {
				manager.setBaseFile(new File(getIntegFolder()));
				if (!manager.getBaseFile().exists()) {
					manager.getBaseFile().createFolder();
				}
			}
		} catch (FileSystemException e) {
			Assert.fail();
		}
	}
}
