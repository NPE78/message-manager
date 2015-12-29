package com.synaptix.mm.supervision.utils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.server.implem.DefaultMMAgent;
import com.synaptix.mm.server.implem.DefaultTestMMServerModule;
import com.synaptix.mm.server.it.AbstractMMTest;
import com.synaptix.mm.supervision.model.AgentInfoDto;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 29/10/2015.
 */
public class SupervisionUtilsTest extends AbstractMMTest {

	@Test
	public void testGetAgentInfo() throws Exception {

		MMDictionary dictionary = getInstance(MMDictionary.class);

		List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo(null);
		Assert.assertEquals(3, agentInfoDtoList.size()); // with RetryAgent

		MyMessage message = new MyMessage();

		Assert.assertEquals(0, message.count);

		ProcessEngine.handle(MyMMAgent.class.getSimpleName(), message);
		ProcessEngine.handle(MySingletonMMAgent.class.getSimpleName(), message);
		ProcessEngine.handle(MySingletonMMAgent.class.getSimpleName(), message);

		waitIntegrator();

		Assert.assertEquals(3, message.count);

	}

	@Override
	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new DefaultTestMMServerModule() {

			@Override
			protected void configureTestModule() {
				bindAgent(MyMMAgent.class, 5, 10);
				bindAgent(MySingletonMMAgent.class, 1, 1);
			}
		};
	}

	private static class MyMMAgent extends DefaultMMAgent {

		private MyMMAgent() {
			super("MyMMAgent");
		}

		@Override
		public void process(Object messageObject) {
			List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo("MyMMAgent");
			Assert.assertEquals(1, agentInfoDtoList.size());

			AgentInfoDto agentInfoDto = agentInfoDtoList.get(0);
			Assert.assertEquals("MyMMAgent", agentInfoDto.getName());
			Assert.assertTrue(agentInfoDto.isBusy());
			Assert.assertTrue(agentInfoDto.isAvailable());
			Assert.assertFalse(agentInfoDto.isOverloaded());
			Assert.assertEquals(new Integer(1), agentInfoDto.getNbWorking());
			Assert.assertEquals(new Integer(0), agentInfoDto.getNbWaiting());

			((MyMessage) messageObject).count++;
		}
	}

	private static class MySingletonMMAgent extends DefaultMMAgent {

		private MySingletonMMAgent() {
			super("MySingletonMMAgent");
		}

		@Override
		public void process(Object messageObject) {
			CountDownLatch cdl = new CountDownLatch(1);
			try {
				cdl.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (SupervisionUtilsTest.class) {
				List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo("MySingletonMMAgent");
				Assert.assertEquals(1, agentInfoDtoList.size());

				AgentInfoDto agentInfoDto = agentInfoDtoList.get(0);
				Assert.assertEquals("MySingletonMMAgent", agentInfoDto.getName());

				if (agentInfoDto.getNbWaiting() == 1) {
					Assert.assertTrue(agentInfoDto.isBusy());
					Assert.assertTrue(agentInfoDto.isAvailable());
					Assert.assertTrue(agentInfoDto.isOverloaded());
					Assert.assertEquals(new Integer(1), agentInfoDto.getNbWorking());
					Assert.assertEquals(new Integer(1), agentInfoDto.getNbWaiting());
				} else {
					Assert.assertTrue(agentInfoDto.isBusy());
					Assert.assertTrue(agentInfoDto.isAvailable());
					Assert.assertFalse(agentInfoDto.isOverloaded());
					Assert.assertEquals(new Integer(1), agentInfoDto.getNbWorking());
					Assert.assertEquals(new Integer(0), agentInfoDto.getNbWaiting());
				}

				((MyMessage) messageObject).count++;
			}
		}
	}

	public static class MyMessage implements Serializable {

		int count;

	}
}
