package com.synaptix.mm.server.unit;

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
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.synaptix.mm.server.IServer;
import com.synaptix.mm.server.MMServer;
import com.synaptix.mm.server.guice.MMServerModule;
import com.synaptix.mm.server.helper.FSHelper;
import com.synaptix.mm.server.helper.ServerHelper;
import com.synaptix.mm.server.implem.DefaultTestMMServerModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public final class MainIntegratorBoot {

	private static final int INTEGRATOR_JETTY_PORT = 8000;

	private static final Log LOG = LogFactory.getLog(MainIntegratorBoot.class);

	public static void main(String[] args) {

		ServerHelper.configureServer();

		Injector injector = createInjector(MainIntegratorBoot.class.getClassLoader().getResource("integrator.properties"), null);

		IServer server = injector.getInstance(MMServer.class);
		server.start(FSHelper.getIntegFolder());

		// MainIntegratorHelper.createJetty(new IJettyStarted() {
		// @Override
		// public void jettyStarted(Server server, WebAppContext context) {
		// }
		// });
	}

	/**
	 * @param integratorTestModule optional
	 * @return the injector instance
	 */
	public static Injector createInjector(URL properties, AbstractModule integratorTestModule) {
		Injector injector = null;
		Properties p = new Properties();
		try {
			if (properties != null) {
				try {
					p.load(new FileReader(properties.getFile())); //$NON-NLS-1$
				} catch (Exception e) {
					LOG.error("", e);
				}
			}
			String trmt = p.getProperty("config.trmt_engine", "TRMT_LOCAL");

			if (integratorTestModule != null) {
				injector = Guice.createInjector(getInjectorStage(), Modules.override(Modules.combine(new MMServerModule(trmt), new DefaultTestMMServerModule())).with(integratorTestModule));
			} else {
				injector = Guice.createInjector(getInjectorStage(), Modules.combine(new MMServerModule(trmt), new DefaultTestMMServerModule()));
			}
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

	public static Stage getInjectorStage() {
		return Stage.DEVELOPMENT;
	}

	private interface IJettyStarted {

		void jettyStarted(Server server, WebAppContext context);

	}
}
