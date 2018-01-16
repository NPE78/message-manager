package com.talanlabs.mm.server.delegate;

import com.talanlabs.mm.server.exception.ContentNotFetchedException;
import com.talanlabs.mm.server.exception.ContentNotSavedException;
import com.talanlabs.mm.shared.model.IFSMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * Created by NicolasP on 06/01/2016.
 */
public class FluxContentManager {

	private static final Log LOG = LogFactory.getLog(FluxContentManager.class);

	/**
	 * Get the content of a test flux
	 */
	public String getTestFluxContent(String id) throws ContentNotFetchedException {
		try {
			return readFile(VFS.getManager().getBaseFile().getChild("testFlux").resolveFile(id));
		} catch (Exception e) {
			throw new ContentNotFetchedException("Exception raised", e);
		}
	}

	/**
	 * Get the content of a message
	 */
	public String getContent(IFSMessage message) throws ContentNotFetchedException {
		if (message == null) {
			throw new ContentNotFetchedException("Message is null");
		}
		if (message.getMessageType() == null) {
			throw new ContentNotFetchedException("Message type is not provided, it is mandatory to find in the right folder");
		}
		FileFinder fileFinder = new FileFinder(message.getName(), message.getFolder(), message.getId().toString());
		try {
			boolean exists = fileFinder.find();
			if (!exists) {
				throw new FileNotFoundException(fileFinder.getPath());
			}
			return readFile(fileFinder.foundFile);
		} catch (Exception e) {
			throw new ContentNotFetchedException("Exception raised", e);
		}
	}

	/**
	 * Set the content of a test flux
	 */
	public void setTestFluxContent(String content, String id) throws ContentNotSavedException {
		try {
			FileObject file = VFS.getManager().getBaseFile().resolveFile("testFlux").resolveFile(id);
			file.getParent().createFolder();
			writeFile(file, content);
		} catch (Exception e) {
			throw new ContentNotSavedException("Couldn't write into file", e);
		}
	}

	/**
	 * Set the content of a message
	 */
	public void setContent(String content, IFSMessage message) throws ContentNotSavedException {
		if (message == null) {
			throw new ContentNotSavedException("Message is null");
		}
		if (message.getMessageType() == null) {
			throw new ContentNotSavedException("Message type is not provided, it is mandatory to find in the right folder");
		}
		FileFinder fileFinder = new FileFinder(message.getName(), message.getFolder(), message.getId().toString());
		try {
			fileFinder.find();
			if (fileFinder.isArchived) {
				throw new ContentNotSavedException("Cannot write in an archive");
			}
			writeFile(fileFinder.foundFile, content);
		} catch (Exception e) {
			throw new ContentNotSavedException("Couldn't write into file", e);
		}
	}

	private String readFile(FileObject file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try (InputStream inputStream = file.getContent().getInputStream();
			 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(line);
			}
		}
		return StringUtils.trimToNull(sb.toString());
	}

	private void writeFile(FileObject file, String content) throws IOException {
		if (!file.isWriteable()) {
			throw new IOException("Can't write into file");
		}
		try (OutputStream outputStream = file.getContent().getOutputStream()) {
			if (content != null) {
				outputStream.write(content.getBytes(Charset.forName("UTF-8"))); //$NON-NLS-1$
			}
		}
	}

	private class FileFinder {

		private final String rootFolder;

		private final String folder;

		private final String filename;

		private final boolean canBeArchived;

		private boolean isArchived;

		private FileObject foundFile;

		FileFinder(String messageTypeName, String folder, String filename) {
			this.rootFolder = messageTypeName + File.separator;
			this.folder = folder != null ? rootFolder + folder + File.separator : rootFolder;
			this.filename = filename;
			this.canBeArchived = folder != null;
		}

		private boolean find() throws IOException {
			boolean myIsArchived = false;
			FileObject myFoundFile = null;

			String myFilename = this.filename;

			DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();
			try {
				myFoundFile = fsManager.resolveFile(getPath());
			} catch (FileSystemException e) {
				LOG.error("", e);
			}

			if (canBeArchived && (myFoundFile == null || !myFoundFile.exists())) {
                String[] fs;
			    if (File.separatorChar == '\\') {
			        fs = folder.split("\\\\");
                } else {
                    fs = folder.split(File.separator);
                }
				if (fs.length >= 1) {
					String path = getPath(fs);
					String tgzName = fs[fs.length - 1];
					if (LOG.isDebugEnabled()) {
						LOG.debug("Looking in " + path + ", " + tgzName + ", " + myFilename);
					}
					FileObject archivedFile = findFileInArchive(path, tgzName, myFilename);
					if (archivedFile != null) {
						myIsArchived = true;
						myFoundFile = archivedFile;
					}
				}
			}

			this.isArchived = myIsArchived;
			this.foundFile = myFoundFile;

			return myFoundFile != null && (myFoundFile.exists() || isArchived);
		}

		private String getPath(String[] fs) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < fs.length - 1; i++) {
				sb.append(fs[i]).append(File.separator);
			}
			return sb.toString();
		}

		private FileObject findFileInArchive(String archiveFile, String tgzName, String filename) throws IOException {
			DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();

			FileObject jarFile;
			try {
				String uri = "tgz:" + archiveFile + tgzName + ".tgz" + "!" + File.separator + tgzName + File.separator;
				if (LOG.isDebugEnabled()) {
					LOG.debug("base file is " + fsManager.getBaseFile().getURL().getPath());
					LOG.debug(uri);
				}
				jarFile = fsManager.resolveFile(uri);
			} catch (FileSystemException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("", e);
				}
				return null;
			}

			FileObject[] children = jarFile.getChildren();
			for (FileObject child : children) {
				String baseName = child.getName().getBaseName();
				if (filename.equals(baseName)) {
					return child;
				}
			}
			return null;
		}

		public String getPath() {
			return folder + filename;
		}
	}
}
