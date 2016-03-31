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
	 * The name of the message type, should be unique
	 */
	void setName(String name);

	/**
	 * The message way of the messages: IN or OUT
	 */
	MessageWay getMessageWay();

	/**
	 * The message way of the messages: IN or OUT
	 */
	void setMessageWay(MessageWay messageWay);

	/**
	 * The recycling deadline in minutes
	 */
	Integer getRecyclingDeadline();

	/**
	 * The recycling deadline in minutes
	 */
	void setRecyclingDeadline(Integer recyclingDeadline);

}
