package com.talanlabs.mm.server.model;

import com.talanlabs.mm.shared.model.IMessage;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.processmanager.messages.flux.AbstractFlux;
import java.io.Serializable;
import java.time.Instant;

public abstract class AbstractMMFlux extends AbstractFlux implements IMessage {

    /**
     * The process context contains a list of errors for this message and this message only
     */
    private final ProcessContext processContext;

    private Serializable id;
    private IMessageType messageType;
    private MessageStatus messageStatus;
    private Instant nextProcessingDate;
    private Instant deadlineDate;
    private Instant firstProcessingDate;


    public AbstractMMFlux() {
        processContext = new ProcessContext();
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    @Override
    public IMessageType getMessageType() {
        return messageType;
    }

    @Override
    public void setMessageType(IMessageType messageType) {
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
