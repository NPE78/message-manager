package com.synaptix.mm.shared.model;

import java.time.Instant;

import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IMessage {

	/**
	 * The type of the message
	 */
	IMessageType getMessageType();

	/**
	 * The status of the message. Uses a kind of workflow to know what to do next
	 */
	MessageStatus getMessageStatus();

	/**
	 * In case of a message which needs to be recycled, this is the date the message can be recycled again
	 */
	Instant getNextProcessingDate();

	/**
	 * If the message still hasn't been integrated or sent at the deadline date, it will be definitely rejected
	 */
	Instant getDeadlineDate();

	/**
	 * This date is used to compute the deadline date
	 */
	Instant getCreatedDate();

}
