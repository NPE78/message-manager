package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.synaptix.pmgr.core.apis.Engine;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

import junit.framework.Assert;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class ITLaunchIntegrator extends AbstractMMTest {

	private static int randomInt;

	@Test
	public void test1() {

		Assert.assertNotNull(getServer());

		randomInt = new Random().nextInt();
		getInstance(AgentTest.class).work(randomInt, ProcessEngine.getInstance());


		//test timeout
		getServer().setTimeoutSeconds(2);
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
				cdl.await(3000, TimeUnit.SECONDS);
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
