package com.synaptix.mm.engine.model;

import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class SimpleMessageType implements IMessageType {

	@Override
	public String getName() {
		return "SimpleMessageType";
	}

	@Override
	public MessageWay getMessageWay() {
		return MessageWay.IN;
	}

	@Override
	public Integer getRecyclingDeadline() {
		return 120;
	}
}
