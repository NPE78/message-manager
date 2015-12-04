package com.synaptix.mm.shared.model;

import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IMessageType {

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

}
