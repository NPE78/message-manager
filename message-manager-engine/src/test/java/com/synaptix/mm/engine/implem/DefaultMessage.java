package com.synaptix.mm.engine.implem;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.shared.model.IFSMessage;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMessage implements IFSMessage {

	private static final Log LOG = LogFactory.getLog(DefaultMessage.class);

	private final Serializable id;

	private final IMessageType messageType;

	private final Instant firstProcessingDate;

	private MessageStatus messageStatus;

	private Instant nextProcessingDate;

	private Instant deadlineDate;

	private String folder;

	private File file;

	private String content;

	public DefaultMessage(String messageType) {
		this(messageType, null);
	}

	public DefaultMessage(String messageType, Serializable id) {
		super();

		this.id = id == null ? UUID.randomUUID() : id;
		this.messageType = messageType != null ? new DefaultMessageType(messageType) : null;
		this.firstProcessingDate = id == null ? null : Instant.now().minus(2, ChronoUnit.MINUTES);
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public IMessageType getMessageType() {
		return messageType;
	}

	@Override
	public void setMessageType(IMessageType messageType) {
		LOG.error("Can not set messageType");
	}

	@Override
	public MessageStatus getMessageStatus() {
		return messageStatus;
	}

	@Override
	public void setMessageStatus(MessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}

	@Override
	public Instant getNextProcessingDate() {
		return nextProcessingDate;
	}

	@Override
	public void setNextProcessingDate(Instant nextProcessingDate) {
		this.nextProcessingDate = nextProcessingDate;
	}

	@Override
	public Instant getDeadlineDate() {
		return deadlineDate;
	}

	@Override
	public void setDeadlineDate(Instant deadlineDate) {
		this.deadlineDate = deadlineDate;
	}

	@Override
	public Instant getFirstProcessingDate() {
		return firstProcessingDate;
	}

	@Override
	public void setFirstProcessingDate(Instant firstProcessingDate) {
		LOG.error("Can not set firstProcessingDate");
	}

	@Override
	public String getFolder() {
		return folder;
	}

	@Override
	public void setFolder(String folder) {
		this.folder = folder;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}
}
