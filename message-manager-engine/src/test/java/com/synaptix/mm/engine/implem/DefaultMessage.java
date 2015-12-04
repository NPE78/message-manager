package com.synaptix.mm.engine.implem;

import java.util.Date;

import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.shared.model.IMessage;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMessage implements IMessage {

	private final IMessageType messageType;

	private final Date createdDate;

	private MessageStatus messageStatus;

	private Date nextProcessingDate;

	private Date deadlineDate;

	public DefaultMessage(String messageType) {
		super();

		this.messageType = new DefaultMessageType(messageType);
		this.createdDate = new Date();
	}

	@Override
	public IMessageType getMessageType() {
		return messageType;
	}

	@Override
	public MessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(MessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}

	@Override
	public Date getNextProcessingDate() {
		return nextProcessingDate;
	}

	public void setNextProcessingDate(Date nextProcessingDate) {
		this.nextProcessingDate = nextProcessingDate;
	}

	@Override
	public Date getDeadlineDate() {
		return deadlineDate;
	}

	public void setDeadlineDate(Date deadlineDate) {
		this.deadlineDate = deadlineDate;
	}

	@Override
	public Date getCreatedDate() {
		return createdDate;
	}
}
