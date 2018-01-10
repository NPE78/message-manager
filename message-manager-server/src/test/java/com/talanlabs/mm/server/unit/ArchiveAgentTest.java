package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.agent.BatchArchiveAgent;
import com.talanlabs.mm.server.agent.DefaultMMImportAgentTest;
import com.talanlabs.mm.server.exception.BatchArchiveException;
import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.server.model.DefaultIntegConfig;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 04/04/2016.
 */
public class ArchiveAgentTest extends AbstractMMTest {

    @Test
    public void testArchiveAgent() throws Exception {
        MyAgent mmAgent = new MyAgent();
        mmAgent.register("test", 5, 2000, new File(FSHelper.getIntegFolder()));

        DefaultIntegConfig config = new DefaultIntegConfig("message-manager");
        config.setIntegPort(0);
        BatchArchiveAgent agent = new BatchArchiveAgent(config);

        URL script = getClass().getClassLoader().getResource("dirarch");

        startIntegrator();

        FileObject fileDestination = VFS.getManager().getBaseFile().resolveFile("archive");
        if (fileDestination.exists()) {
            fileDestination.delete();
        }

        Assert.assertEquals(fileDestination.getURL(), agent.getScript(script));

        // test with files
        File file = createFile();

        File archivePath = new File(file.getParentFile().getAbsolutePath() + "/archiveTest" + UUID.randomUUID().toString() + "/");
        archivePath.mkdirs();
        file.renameTo(new File(archivePath.getAbsolutePath() + "/" + file.getName()));

        agent.work(archivePath.getAbsolutePath(), "test");
        if (SystemUtils.IS_OS_LINUX) { // windows can't play script
            System.out.println(file);
            Assert.assertFalse(file.exists());
        }

        file = createFile();
        file.renameTo(new File(archivePath.getAbsolutePath() + "/" + file.getName()));
        agent.work(null, "test");
        if (SystemUtils.IS_OS_LINUX) { // windows can't play script
            System.out.println(file);
            Assert.assertFalse(file.exists());
        }
    }

    @Test(expected = BatchArchiveException.class)
    public void testException() throws MalformedURLException {
        DefaultIntegConfig config = new DefaultIntegConfig("message-manager");
        config.setIntegPort(0);
        BatchArchiveAgent agent = new BatchArchiveAgent(config);

        startIntegrator();

        agent.getScript(new URL("file:\\invalidPath"));
    }

    private File createFile() throws IOException {
        File file = File.createTempFile("test_archive", "txt");
        Calendar calendar = Calendar.getInstance();
        calendar.roll(4, Calendar.DAY_OF_WEEK);
        file.setLastModified(calendar.getTimeInMillis());
        return file;
    }

    private class MyAgent extends DefaultMMImportAgentTest<ArchiveTest> {

        MyAgent() {
            super(ArchiveTest.class);
        }
    }

    private class ArchiveTest extends AbstractMMImportFlux {
    }
}
