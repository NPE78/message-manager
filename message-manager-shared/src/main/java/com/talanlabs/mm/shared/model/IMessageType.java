package com.talanlabs.mm.shared.model;

import com.talanlabs.mm.shared.model.domain.MessageWay;
import java.io.Serializable;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IMessageType extends Serializable {

    /**
     * The name of the message type, should be unique
     */
    String getName();

    /**
     * The message way of the messages: IN or OUT
     */
    MessageWay getMessageWay();

    /**
     * The recycling deadline in minutes
     */
    Integer getRecyclingDeadline();

    static IMessageType of(String name, MessageWay messageWay) {
        return IMessageType.of(name, messageWay, 60 * 24); // 1 day
    }

    static IMessageType of(String name, MessageWay messageWay, Integer recyclingDeadline) {
        return new IMessageType() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public MessageWay getMessageWay() {
                return messageWay;
            }

            @Override
            public Integer getRecyclingDeadline() {
                return recyclingDeadline;
            }
        };
    }

}
