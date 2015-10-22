package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import java.util.Random;

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
//		ProcessEngine.handle(AgentTest.class.getSimpleName(), "message");
	}

	@Override
	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new TestIntegratorServletModule();
	}

	private class TestIntegratorServletModule extends AbstractSynaptixIntegratorServletModule {

		@Override
		protected void configure() {
			bindAgent(AgentTest.class, 5, 10);
		}
	}

	private static class AgentTest implements ProcessingChannel.Agent {

		@Override
		public void work(Object o, Engine engine) {
			Assert.assertEquals(randomInt, o);
			System.out.println(o);
		}
	}
}
