package com.synaptix.mm.server.unit;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.server.MMServer;
import com.synaptix.mm.server.it.AbstractMMTest;
import com.synaptix.pmgr.core.apis.Engine;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMServerTest extends AbstractMMTest {

	private static int randomInt;

	@Test
	public void test1() {

		Assert.assertNotNull(getServer());

		randomInt = new Random().nextInt();
		getInstance(AgentTest.class).work(randomInt, ProcessEngine.getInstance());

		//test timeout
		((MMServer) getServer()).setTimeoutSeconds(2);
		ProcessEngine.handle(SlowAgentTest.class.getSimpleName(), "message");

		getServer().stop(); // will be stopped anyway, but this is to test the double stop
	}

	@Override
	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new TestIntegratorServletModule();
	}

	private static class AgentTest implements ProcessingChannel.Agent {

		@Override
		public void work(Object o, Engine engine) {
			Assert.assertEquals(randomInt, o);
			System.out.println(o);
		}
	}

	private static class SlowAgentTest implements ProcessingChannel.Agent {

		@Override
		public void work(Object o, Engine engine) {
			boolean hasBeenStopped = false;
			try {
				CountDownLatch cdl = new CountDownLatch(1);
				cdl.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				hasBeenStopped = true; // never called if the integrator really stopped
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Assert.assertTrue(hasBeenStopped);
			}
//			Assert.assertEquals(randomInt, o);
		}
	}

	private class TestIntegratorServletModule extends AbstractSynaptixIntegratorServletModule {

		@Override
		protected void configure() {
			bindAgent(AgentTest.class, 5, 10);
			bindAgent(SlowAgentTest.class, 5, 10);
		}
	}
}
