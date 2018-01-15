package com.talanlabs.mm.server.model;

import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.domain.MessageWay;

public class DefaultMessageType implements IMessageType {

    private String name;
    private MessageWay messageWay;
    private Integer recyclingDeadline;

    public DefaultMessageType(String name, MessageWay messageWay) {
        this.name = name;
        this.messageWay = messageWay;
        this.recyclingDeadline = 60 * 24; // 1 day
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MessageWay getMessageWay() {
        return messageWay;
    }

    public void setMessageWay(MessageWay messageWay) {
        this.messageWay = messageWay;
    }

    @Override
    public Integer getRecyclingDeadline() {
        return recyclingDeadline;
    }

    public void setRecyclingDeadline(Integer recyclingDeadline) {
        this.recyclingDeadline = recyclingDeadline;
    }
}
