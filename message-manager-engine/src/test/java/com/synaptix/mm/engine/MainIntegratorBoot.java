package com.synaptix.mm.engine;

import java.io.FileReader;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.synaptix.mm.engine.guice.MMEngineModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MainIntegratorBoot {

	private static final int INTEGRATOR_JETTY_PORT = 8000;
	private static final Log LOG = LogFactory.getLog(MainIntegratorBoot.class);

	public static void main(String[] args) {

		createServer(null);

		// MainIntegratorHelper.createJetty(new IJettyStarted() {
		// @Override
		// public void jettyStarted(Server server, WebAppContext context) {
		// }
		// });
	}

	/**
	 * @param integratorTestModule optional
	 * @return the server instance
	 */
	public static Injector createServer(AbstractModule integratorTestModule) {
		Injector injector = null;
		Properties p = new Properties();
		try {
			URL resource = MainIntegratorBoot.class.getClassLoader().getResource("integrator.properties");
			if (resource != null) {
				p.load(new FileReader(resource.getFile())); //$NON-NLS-1$
			}
			String trmt = p.getProperty("trmt_engine", "TRMT_LOCAL");

			if (integratorTestModule != null) {
				injector = Guice.createInjector(Modules.combine(new MMEngineModule(trmt), integratorTestModule));
			} else {
				injector = Guice.createInjector(new MMEngineModule(trmt));
			}
			MMServer server = injector.getInstance(MMServer.class);

			server.start();

		} catch (Exception e) {
			LOG.error(e, e);
		}

		return injector;
	}

	public static void createJetty(final IJettyStarted jettyStarted) {
		try {
			final Server server = new Server();
			SocketConnector connector = new SocketConnector();
			connector.setPort(INTEGRATOR_JETTY_PORT);
			server.setConnectors(new Connector[]{connector});
			final WebAppContext context = new WebAppContext();
			context.setWar("./src/main/webapp");
			context.setContextPath("/message-manager-engine");
			server.addHandler(context);

			server.addLifeCycleListener(new LifeCycle.Listener() {

				@Override
				public void lifeCycleStopping(LifeCycle event) {
				}

				@Override
				public void lifeCycleStopped(LifeCycle event) {
				}

				@Override
				public void lifeCycleStarting(LifeCycle event) {
				}

				@Override
				public void lifeCycleStarted(LifeCycle event) {
					jettyStarted.jettyStarted(server, context);
				}

				@Override
				public void lifeCycleFailure(LifeCycle event, Throwable cause) {
				}
			});

			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private interface IJettyStarted {

		void jettyStarted(Server server, WebAppContext context);

	}
}
