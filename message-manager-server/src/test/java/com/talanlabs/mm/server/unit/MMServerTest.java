package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.server.MMServer;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.engine.ProcessManager;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMServerTest extends AbstractMMTest {

	private static int randomInt;

	@Test
	public void test1() {

		Assert.assertNotNull(getServer());

        AgentTest agentTest = new AgentTest();
        SlowAgentTest slowAgentTest = new SlowAgentTest();

		randomInt = new Random().nextInt();
		agentTest.register("test", 5);
        slowAgentTest.register("test", 5);

		agentTest.work(randomInt, "test");

		//test timeout
		((MMServer) getServer()).setTimeoutSeconds(2);
		ProcessManager.handle("test", SlowAgentTest.class.getSimpleName(), "message");

		getServer().stop(); // will be stopped anyway, but this is to test the double stop
	}

	private class AgentTest extends AbstractAgent {

        AgentTest() {
            super(AgentTest.class.getSimpleName());
        }

		@Override
		public void work(Serializable o, String engineUuid) {
			Assert.assertEquals(randomInt, o);
			System.out.println(o);
		}
	}

	private class SlowAgentTest extends AbstractAgent {

        SlowAgentTest() {
            super(SlowAgentTest.class.getSimpleName());
        }

        @Override
		public void work(Serializable o, String engineUuid) {
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
}
