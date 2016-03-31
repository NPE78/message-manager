package com.synaptix.mm.shared.model;

import java.io.Serializable;
import java.time.Instant;

import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IMessage {

	/**
	 * The id of the message
	 */
	Serializable getId();

	/**
	 * The type of the message
	 */
	IMessageType getMessageType();

	/**
	 * The type of the message
	 */
	void setMessageType(IMessageType messageType);

	/**
	 * The status of the message. Uses a kind of workflow to know what to do next
	 */
	MessageStatus getMessageStatus();

	/**
	 * The status of the message. Uses a kind of workflow to know what to do next
	 */
	void setMessageStatus(MessageStatus messageStatus);

	/**
	 * In case of a message which needs to be recycled, this is the date the message can be recycled again
	 */
	Instant getNextProcessingDate();

	/**
	 * In case of a message which needs to be recycled, this is the date the message can be recycled again
	 */
	void setNextProcessingDate(Instant nextProcessingDate);

	/**
	 * If the message still hasn't been integrated or sent at the deadline date, it will be definitely rejected
	 */
	Instant getDeadlineDate();

	/**
	 * If the message still hasn't been integrated or sent at the deadline date, it will be definitely rejected
	 */
	void setDeadlineDate(Instant deadlineDate);

	/**
	 * This date is used to compute the deadline date
	 */
	Instant getFirstProcessingDate();

	/**
	 * This date is used to compute the deadline date
	 */
	void setFirstProcessingDate(Instant firstProcessingDate);

}
