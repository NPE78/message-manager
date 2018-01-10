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
