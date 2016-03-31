package com.synaptix.mm.server.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.synaptix.mm.shared.model.IIntegConfig;
import com.synaptix.pmgr.core.apis.Engine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;
import com.synaptix.pmgr.trigger.injector.IInjector;

/**
 * Created by NicolasP on 06/01/2016.
 */
public class BatchArchiveAgent implements ProcessingChannel.Agent {

	private static final Log LOG = LogFactory.getLog(BatchArchiveAgent.class);

	@Inject(optional = true)
	private Set<Class<? extends IInjector>> injectorSet;

	@Inject
	private Injector injector;

	@Inject
	private IIntegConfig config;

	private boolean initialized;

	@Override
	public void work(Object o, Engine engine) {

		URL script = getClass().getResource("dirarch");
		if (script == null) {
			LOG.error("DIRARCH script not found!");
		} else {

			script = getScript(script);

			String scriptPath = new File(script.getPath()).getPath();

			if (o instanceof String && !"Cron".equals(o)) {
				archiveAll(scriptPath, (String) o);
			} else {
				for (Class<? extends IInjector> injectorClass : injectorSet) {
					IInjector integratorInjector = this.injector.getInstance(injectorClass);
					File workDir = integratorInjector.getWorkDir();

					archiveAll(scriptPath, workDir.getAbsolutePath());
				}
			}
		}
	}

	private URL getScript(URL script) {
		try {
			FileSystemManager manager = VFS.getManager();
			FileObject fileObject = manager.resolveFile(new File(script.getFile()), script.toURI().toString());
			LOG.info(fileObject.toString());
			FileObject fileDestination = manager.getBaseFile().resolveFile("archive");

			if (!fileDestination.exists() || !initialized) {
				initScript(script, fileDestination);
			}

			return fileDestination.getURL();
		} catch (Exception e) {
			LOG.error("Error copying script", e);
		}
		return script;
	}

	private void initScript(URL script, FileObject fileDestination) throws IOException, URISyntaxException {
		OutputStream outputStream = fileDestination.getContent().getOutputStream(false); // no append, create a new file
		try (InputStream is = (InputStream) script.getContent(); InputStreamReader inputStreamReader = new InputStreamReader(is)) {
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line;
			while ((line = reader.readLine()) != null) {
				outputStream.write(line.getBytes(Charset.defaultCharset()));
				outputStream.write(10); // new line
			}
			outputStream.close();
		}

		File dest = new File(fileDestination.getURL().toURI());
		if (!dest.setExecutable(true, false)) {
			LOG.warn("Archive file has not been set to executable");
		}

		initialized = true;
	}

	private void archiveAll(String path, String workDir) {
		archiveFolder(path, workDir);
		archiveSubFolder(path, workDir, "accepted");
		archiveSubFolder(path, workDir, "rejected");
		archiveSubFolder(path, workDir, "retry");
		archiveSubFolder(path, workDir, "archive");
	}

	private void archiveSubFolder(String path, String workDir, String subFolder) {
		archiveFolder(path, workDir + File.separator + subFolder);
	}

	private void archiveFolder(String scriptPath, String folder) {
		try {
			String cmd = scriptPath + " " + folder + " " + config.getIntegHost() + ":" + config.getIntegPort() + "/lisea-app-integ/rest/arch/archive";
			LOG.debug(cmd);
			Process process = Runtime.getRuntime().exec(cmd);

			if (LOG.isDebugEnabled()) {
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					LOG.debug("[ARCHIVE SCRIPT] " + line);
				}
			}
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			LOG.error("BATCH ARCHIVE ERROR", e);
		}
	}
}
