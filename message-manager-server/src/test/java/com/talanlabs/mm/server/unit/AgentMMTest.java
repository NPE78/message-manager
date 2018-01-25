package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.server.AbstractMMImportAgent;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.helper.TestUtils;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.shared.exceptions.AgentException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentMMTest extends AbstractMMTest {

    @Test
    public void testMMAgent() throws InterruptedException, IOException {
        final Map<String, Boolean> checks = new HashMap<>();

        File file = new File(FSHelper.getIntegFolder(), "MyFlux/fluxTest");
        if (file.exists()) {
            Assertions.assertThat(file.delete()).isTrue();
        }

        MyMMAgent agent = new MyMMAgent() {
            @Override
            protected void acceptMessage() {
                checks.put("ACCEPTED", true);
            }
        };
        agent.register("test", 5, 200, new File(FSHelper.getIntegFolder()));
        Assertions.assertThat(agent.getWorkDir()).isEqualTo(file.getParentFile());

        startIntegrator();

        Assertions.assertThat(file.createNewFile()).isTrue();
        file.deleteOnExit();
        TestUtils.sleep(500);

        Assertions.assertThat(checks.get("ACCEPTED")).isTrue();

        waitIntegrator(10);
    }

    @Test(expected = AgentException.class)
    public void testMMAgentException() {
        try {
            MyMMAgent agent = new MyMMAgent();
            agent.register("test", 5);
        } finally {
            PM.shutdownEngine("test");
        }
    }

    private class MyMMAgent extends AbstractMMImportAgent<MyFlux> {

        MyMMAgent() {
            super(MyFlux.class);
        }

        @Override
        protected MyFlux createFlux() {
            return new MyFlux();
        }

        @Override
        protected void saveOrUpdateMessage(MyFlux message) {

        }

        @Override
        protected void acceptMessage() {

        }

        @Override
        protected void rejectMessage() {

        }

        @Override
        protected void saveMessageError(IProcessError processError, ErrorImpact errorImpact) {

        }

        @Override
        public void process(MyFlux message) {

        }

        @Override
        public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {

        }
    }

    private static class MyFlux extends AbstractMMImportFlux {

        MyFlux() {
            setId(UUID.randomUUID());
        }
    }
}
