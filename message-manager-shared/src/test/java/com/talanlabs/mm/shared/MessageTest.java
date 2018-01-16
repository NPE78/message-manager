package com.talanlabs.mm.shared;

import com.talanlabs.mm.shared.model.IMessage;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MessageTest {

    @Test
    public void testMessage() {
        Instant now = Instant.now();
        Instant next = now.plus(5, ChronoUnit.MINUTES);
        Instant deadline = now.plus(1, ChronoUnit.DAYS);

        MyMessage message = new MyMessage();
        message.setId("id");
        message.setMessageType(IMessageType.of("test", MessageWay.IN));
        message.setMessageStatus(MessageStatus.TO_BE_PROCESSED);
        message.setNextProcessingDate(next);
        message.setDeadlineDate(deadline);
        message.setFirstProcessingDate(now);

        Assertions.assertThat(message.getId()).isEqualTo("id");
        Assertions.assertThat(message.getName()).isEqualTo("test");
        Assertions.assertThat(message.getMessageType().getName()).isEqualTo("test");
        Assertions.assertThat(message.getMessageType().getMessageWay()).isEqualTo(MessageWay.IN);
        Assertions.assertThat(message.getMessageType().getRecyclingDeadline()).isEqualTo(60 * 24);
        Assertions.assertThat(message.getNextProcessingDate()).isEqualTo(next);
        Assertions.assertThat(message.getDeadlineDate()).isEqualTo(deadline);
        Assertions.assertThat(message.getFirstProcessingDate()).isEqualTo(now);

    }

    private class MyMessage implements IMessage {

        private Serializable id;
        private IMessageType messageType;
        private MessageStatus messageStatus;
        private Instant nextProcessingDate;
        private Instant deadlineDate;
        private Instant firstProcessingDate;

        @Override
        public Serializable getId() {
            return id;
        }

        void setId(Serializable id) {
            this.id = id;
        }

        @Override
        public IMessageType getMessageType() {
            return messageType;
        }

        void setMessageType(IMessageType messageType) {
            this.messageType = messageType;
        }

        @Override
        public MessageStatus getMessageStatus() {
            return messageStatus;
        }

        @Override
        public void setMessageStatus(MessageStatus messageStatus) {
            this.messageStatus = messageStatus;
        }

        @Override
        public Instant getNextProcessingDate() {
            return nextProcessingDate;
        }

        @Override
        public void setNextProcessingDate(Instant nextProcessingDate) {
            this.nextProcessingDate = nextProcessingDate;
        }

        @Override
        public Instant getDeadlineDate() {
            return deadlineDate;
        }

        @Override
        public void setDeadlineDate(Instant deadlineDate) {
            this.deadlineDate = deadlineDate;
        }

        @Override
        public Instant getFirstProcessingDate() {
            return firstProcessingDate;
        }

        @Override
        public void setFirstProcessingDate(Instant firstProcessingDate) {
            this.firstProcessingDate = firstProcessingDate;
        }
    }
}
