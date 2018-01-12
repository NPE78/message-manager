package com.talanlabs.mm.server;

import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.helper.ServerHelper;
import com.talanlabs.mm.server.helper.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class AbstractMMTest {

	protected static final Log LOG = LogFactory.getLog(AbstractMMTest.class);

	private boolean started;

    private IServer server;

	@Before
	public void init() throws BaseEngineCreationException {
		ServerHelper.configureServer();

        server = new MMServer("test", TestUtils.getErrorPath());

		if (autoStartIntegrator()) {
			startIntegrator();
		}
	}

	protected String getIntegFolder() {
		return FSHelper.getIntegFolder();
	}

	/**
	 * Start the integrator
	 */
	protected final void startIntegrator() {
		server.start(getIntegFolder());
		this.started = true;
	}

	public boolean autoStartIntegrator() {
		return false;
	}

	/**
	 * Start the integrator by using {@link #startIntegrator()}
	 */
	protected final IServer getServer() {
		return server;
	}

	@After
	public void stopIntegrator() {
		if (started) {
			if (server != null) {
				server.stop();
			}
		}
	}

	protected final void waitIntegrator() {
		waitIntegrator(10);
	}

	protected final void waitIntegrator(int timeout) {
		final CountDownLatch cdl = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			while (server.isRunning()) {
                waitSec();
            }
			cdl.countDown();
		});
		thread.setDaemon(false);
		thread.start();

		try {
			if (cdl.await(timeout, TimeUnit.SECONDS)) {
				LOG.info("Stop called: waited in time");
			} else {
				LOG.info("Stop called: timeout");
				LOG.info(server.runningSet().toString());
			}
		} catch (InterruptedException e) {
			LOG.error("Stop called", e);
		}
	}

    private void waitSec() {
        try {
            CountDownLatch cdl = new CountDownLatch(1);
            cdl.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Stop waiter interrupted", e);
        }
    }

    public final String readFile(String filename) {
		StringBuilder content = new StringBuilder();
		try {
			Path file = Paths.get(filename);
			BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
			String line;
			while ((line = reader.readLine()) != null) {
				if (content.length() > 0) {
					content.append("/n");
				}
				content.append(line);
			}
		} catch (IOException e) {
			LOG.error("IOException", e);
		}
		return content.toString();
	}
}
