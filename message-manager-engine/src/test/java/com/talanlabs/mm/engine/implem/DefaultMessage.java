package com.talanlabs.mm.engine.implem;

import com.talanlabs.mm.engine.model.DefaultMessageType;
import com.talanlabs.mm.shared.model.IFSMessage;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMessage implements IFSMessage {

	private final Serializable id;

	private final IMessageType messageType;

	private MessageStatus messageStatus;

	private Instant nextProcessingDate;

	private Instant deadlineDate;

	private Instant firstProcessingDate;

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
		this.firstProcessingDate = firstProcessingDate;
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
    public String getFolder() {
        return folder;
    }

    @Override
    public void setFolder(String folder) {
        this.folder = folder;
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
