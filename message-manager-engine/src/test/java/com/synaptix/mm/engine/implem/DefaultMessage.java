package com.synaptix.mm.engine.implem;

import java.time.Instant;

import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.shared.model.IMessage;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMessage implements IMessage {

	private final IMessageType messageType;

	private final Instant createdDate;

	private MessageStatus messageStatus;

	private Instant nextProcessingDate;

	private Instant deadlineDate;

	public DefaultMessage(String messageType) {
		super();

		this.messageType = new DefaultMessageType(messageType);
		this.createdDate = Instant.now();
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
	public Instant getNextProcessingDate() {
		return nextProcessingDate;
	}

	public void setNextProcessingDate(Instant nextProcessingDate) {
		this.nextProcessingDate = nextProcessingDate;
	}

	@Override
	public Instant getDeadlineDate() {
		return deadlineDate;
	}

	public void setDeadlineDate(Instant deadlineDate) {
		this.deadlineDate = deadlineDate;
	}

	@Override
	public Instant getCreatedDate() {
		return createdDate;
	}
}
