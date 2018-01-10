package com.talanlabs.mm.engine.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultMessageType implements IMessageType {

	private static final Log LOG = LogFactory.getLog(DefaultMessageType.class);

	private final String name;

	private final MessageWay messageWay;

	private Integer recyclingDeadline;

	public DefaultMessageType(String name) {
		this(name, MessageWay.IN);
	}

	public DefaultMessageType(String name, MessageWay messageWay) {
		super();

		this.name = name;
		this.messageWay = messageWay;
		this.recyclingDeadline = 120;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		LOG.error("Can not set name");
	}

	@Override
	public MessageWay getMessageWay() {
		return messageWay;
	}

	@Override
	public void setMessageWay(MessageWay messageWay) {
		LOG.error("Can not set messageWay");
	}

	@Override
	public Integer getRecyclingDeadline() {
		return recyclingDeadline;
	}

	@Override
	public void setRecyclingDeadline(Integer recyclingDeadline) {
		this.recyclingDeadline = recyclingDeadline;
	}
}
