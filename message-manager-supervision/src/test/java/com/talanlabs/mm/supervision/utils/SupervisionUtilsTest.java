package com.talanlabs.mm.supervision.utils;

import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.MM;
import com.talanlabs.mm.server.helper.TestUtils;
import com.talanlabs.mm.server.implem.DefaultMMAgent;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.mm.supervision.model.AgentInfoDto;
import com.talanlabs.processmanager.engine.ProcessManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 29/10/2015.
 */
public class SupervisionUtilsTest extends AbstractMMTest {

    @Test
    public void testGetAgentInfo() throws Exception {
        MyMMAgent myMMAgent = new MyMMAgent();
        myMMAgent.register("test", 5);

        MySingletonMMAgent mySingletonMMAgent = new MySingletonMMAgent();
        mySingletonMMAgent.register("test", 1);

        startIntegrator();

        List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo("test", null);
        Assert.assertTrue(agentInfoDtoList.size() >= 2); // with RetryAgent

        MyFlux1 myFlux1 = new MyFlux1();
        myFlux1.init("test", MyFlux1.class.getSimpleName());

        MM.handle(myFlux1);
        ProcessManager.handle("test", MySingletonMMAgent.class.getSimpleName(), new MyFlux2());
        ProcessManager.handle("test", MySingletonMMAgent.class.getSimpleName(), new MyFlux2());

        TestUtils.sleep(2000);
    }

    private static class MyMMAgent extends DefaultMMAgent<MyFlux1> {

        MyMMAgent() {
            super(MyFlux1.class);
        }

        @Override
        public void process(MyFlux1 messageObject) {
            List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo("test", "MyFlux1");
            Assert.assertEquals(1, agentInfoDtoList.size());

            AgentInfoDto agentInfoDto = agentInfoDtoList.get(0);
            Assert.assertEquals("MyFlux1", agentInfoDto.getName());
            Assert.assertTrue(agentInfoDto.isBusy());
            Assert.assertTrue(agentInfoDto.isAvailable());
            Assert.assertFalse(agentInfoDto.isOverloaded());
            Assert.assertEquals(new Integer(1), agentInfoDto.getNbWorking());
        }
    }

    private static class MySingletonMMAgent extends DefaultMMAgent<MyFlux2> {

        MySingletonMMAgent() {
            super(MyFlux2.class);
        }

        @Override
        public void process(MyFlux2 messageObject) {
            CountDownLatch cdl = new CountDownLatch(1);
            try {
                cdl.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (SupervisionUtilsTest.class) {
                List<AgentInfoDto> agentInfoDtoList = SupervisionUtils.getAgentInfo("test", "MyFlux2");
                Assert.assertEquals(1, agentInfoDtoList.size());

                AgentInfoDto agentInfoDto = agentInfoDtoList.get(0);
                Assert.assertEquals("MyFlux2", agentInfoDto.getName());

                Assert.assertTrue(agentInfoDto.isBusy());
                Assert.assertTrue(agentInfoDto.isAvailable());
                Assert.assertFalse(agentInfoDto.isOverloaded());
                Assert.assertEquals(new Integer(1), agentInfoDto.getNbWorking());
            }
        }
    }

    private static class MyFlux1 extends AbstractMMFlux {
    }

    private static class MyFlux2 extends AbstractMMFlux {
    }
}
