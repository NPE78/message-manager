package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.engine.factory.DefaultProcessErrorFactory;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.MM;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.processmanager.engine.AbstractAgent;
import com.talanlabs.processmanager.engine.ProcessManager;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 21/10/2015.
 */
public class MMTest extends AbstractMMTest {

	private static int randomInt;

	@Test
	public void testServer() {

		Assert.assertNotNull(getServer());

        AgentTest agentTest = new AgentTest();
        SlowAgentTest slowAgentTest = new SlowAgentTest();

		randomInt = new Random().nextInt();
		agentTest.register("test", 5);
        slowAgentTest.register("test", 5);

        startIntegrator();

		agentTest.work(randomInt, "test");

		//test timeout
		((MM) getServer()).setTimeoutSeconds(2);
		ProcessManager.handle("test", SlowAgentTest.class.getSimpleName(), "message");

		getServer().stop(); // will be stopped anyway, but this is to test the double stop
	}

	@Test
    public void testProcessErrorFactory() {
        DefaultProcessErrorFactory processErrorFactory = new DefaultProcessErrorFactory();
        getServer().setProcessErrorFactory(processErrorFactory);
        Assertions.assertThat(MMEngineAddon.getProcessErrorFactory("test") == processErrorFactory).isTrue();

        ProcessManager.getInstance().shutdownEngine("test");
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
