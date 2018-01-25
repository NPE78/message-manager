package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.engine.exception.UnknownDictionaryException;
import com.talanlabs.mm.server.AbstractMMImportAgent;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.helper.TestUtils;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.server.model.IErrorEnum;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
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

public class SimpleAgentMMTest extends AbstractMMTest {

    @Test
    public void testMMAgent() throws InterruptedException, IOException, UnknownDictionaryException {
        final Map<String, Boolean> checks = new HashMap<>();

        File file = new File(FSHelper.getIntegFolder(), "MyFlux/fluxTest");
        if (file.exists()) {
            Assertions.assertThat(file.delete()).isTrue();
        }

        MyMMAgent agent = new MyMMAgent() {
            @Override
            protected void acceptMessage() {
                checks.put("ACCEPTED", true);
                checks.put("FILENAME", getMessage().getId().toString().equals(getMessage().getFile().getName()));
            }
        };
        agent.register("test", 5, 200, new File(FSHelper.getIntegFolder()));

        MMDictionary dictionary = MMEngineAddon.getDictionary("test");

        startIntegrator();

        Assertions.assertThat(file.createNewFile()).isTrue();
        file.deleteOnExit();
        TestUtils.sleep(500);

        SubDictionary agentDictionary = dictionary.getSubsetDictionary(MyFlux.class.getSimpleName());
        Assertions.assertThat(agentDictionary.getErrorMap()).hasSize(MyErrorEnum.values().length); // after being initialized, it should have this size
        Assertions.assertThat(checks.get("ACCEPTED")).isTrue();
        Assertions.assertThat(checks.get("FILENAME")).isTrue();

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

    @Test
    public void testReject() {
        final Map<String, Boolean> checks = new HashMap<>();

        MyMMAgent agent = new MyMMAgent() {
            @Override
            protected void rejectMessage() {
                checks.put("REJECTED", true);
            }
        };
        agent.register("test", 5, 200, new File(FSHelper.getIntegFolder()));

        startIntegrator();

        agent.dispatchMessage("testMessage", "test");

        Assertions.assertThat(checks.get("REJECTED")).isTrue();
    }
//
//    @Test
//    public void testRenameAndMove() {
//        MyMMAgent agent = new MyMMAgent();
//        agent.register("test", 5, 200, new File(FSHelper.getIntegFolder()));
//    }

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

        @Override
        protected void enrichDictionary(SubDictionary subDictionary, String engineUuid) {
            registerErrorEnum(MyErrorEnum.class, subDictionary);
        }
    }

    private static class MyFlux extends AbstractMMImportFlux {

        MyFlux() {
            setId(UUID.randomUUID());
        }
    }

    private enum MyErrorEnum implements IErrorEnum {

        ERROR1(ErrorRecyclingKind.MANUAL), ERROR2(ErrorRecyclingKind.NOT_RECYCLABLE);

        private final ErrorRecyclingKind recyclingKind;

        MyErrorEnum(ErrorRecyclingKind recyclingKind) {
            this.recyclingKind = recyclingKind;
        }

        @Override
        public ErrorRecyclingKind getRecyclingKind() {
            return recyclingKind;
        }
    }
}
