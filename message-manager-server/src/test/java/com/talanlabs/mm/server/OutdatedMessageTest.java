package com.talanlabs.mm.server;

import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.processmanager.engine.PM;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class OutdatedMessageTest extends AbstractMMTest {

    @Test
    public void testOutdatedMessage() {
        MyMMAgent agent = new MyMMAgent();
        agent.register("test", 5, 200, new File(FSHelper.getIntegFolder()));

        MyFlux myFlux = new MyFlux();
        myFlux.init("test");
        Instant now = Instant.now().minus(2, ChronoUnit.DAYS);
        myFlux.setFirstProcessingDate(now);
        myFlux.setMessageStatus(MessageStatus.TO_BE_PROCESSED);

        agent.manageDeadlineDate(myFlux);
        Assertions.assertThat(myFlux.getMessageStatus()).isEqualTo(MessageStatus.REJECTED);

        PM.shutdownEngine("test");
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
