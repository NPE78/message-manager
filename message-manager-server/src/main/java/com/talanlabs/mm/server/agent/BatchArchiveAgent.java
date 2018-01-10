package com.talanlabs.mm.server.agent;

import com.talanlabs.mm.server.exception.BatchArchiveException;
import com.talanlabs.mm.shared.model.IIntegConfig;
import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.gate.Gate;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 * An agent which goal is to archive files
 */
public class BatchArchiveAgent implements Agent {

    private final LogService logService;

    private final IIntegConfig config;

    private boolean initialized;

    public BatchArchiveAgent(IIntegConfig config) {
        logService = LogManager.getLogService(getClass());

        this.config = config;
    }

    @Override
    public void work(Serializable message, String engineUuid) {

        URL script = getClass().getClassLoader().getResource("dirarch");
        if (script == null) {
            logService.error(() -> "DIRARCH script not found!");
        } else {

            script = getScript(script);

            String scriptPath = new File(script.getPath()).getPath();

            if (message instanceof String && !"Cron".equals(message)) {
                archiveAll(scriptPath, (String) message);
            } else {
                ProcessManager.getEngine(engineUuid).getAddon(GateFactory.class).ifPresent(gateFactory -> archiveGate(gateFactory, scriptPath));
            }
        }
    }

    private void archiveGate(GateFactory gateFactory, String scriptPath) {
        gateFactory.getGateList().stream().map(Gate::getEntranceFolder).map(File::getAbsolutePath)
                .forEach(folder -> archiveAll(scriptPath, folder));
    }

    public URL getScript(URL script) {
        try {
            FileSystemManager manager = VFS.getManager();
            FileObject fileObject = manager.resolveFile(new File(script.getFile()), script.toURI().toString());
            logService.info(fileObject::toString);
            FileObject fileDestination = manager.getBaseFile().resolveFile("archive");

            if (!fileDestination.exists() || !initialized) {
                initScript(script, fileDestination);
            }

            return fileDestination.getURL();
        } catch (Exception e) {
            logService.error(() -> "Error copying script", e);
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
            logService.warn(() -> "Archive file has not been set to executable");
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
            String cmd = scriptPath + " " + folder;
            if (config.getIntegPort() != 0) {
                cmd += " " + config.getIntegHost() + ":" + config.getIntegPort() + "/" + config.getIntegApplicationName() + "/rest/arch/archive";
            }
            String shellCmd = cmd;
            logService.debug(() -> shellCmd);
            Process process = Runtime.getRuntime().exec(shellCmd);

            logService.debug(() -> "[ARCHIVE SCRIPT] " + read(process.getInputStream()));
            process.waitFor();
        } catch (BatchArchiveException | IOException | InterruptedException e) {
            logService.error(() -> "BATCH ARCHIVE ERROR", e);
        }
    }

    private String read(InputStream inputStream) {
        try (InputStreamReader isr = new InputStreamReader(inputStream)) {
            return IOUtils.readLines(isr).stream().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new BatchArchiveException(e);
        }
    }
}
