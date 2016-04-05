package com.synaptix.mm.server.injector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import com.google.common.base.CaseFormat;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synaptix.mm.server.exception.InjectorException;
import com.synaptix.mm.shared.model.IFSMessage;
import com.synaptix.mm.shared.model.IMessage;
import com.synaptix.mm.shared.model.IMessageType;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;
import com.synaptix.pmgr.trigger.injector.AbstractMsgInjector;
import com.synaptix.pmgr.trigger.injector.IInjector;
import com.synaptix.tmgr.libs.tasks.filesys.FolderEventTriggerTask;

/**
 * Created by NicolasP on 04/12/2015.
 */
public abstract class AbstractMMInjector extends AbstractMsgInjector implements IInjector {

	private static final Log LOG = LogFactory.getLog(AbstractMMInjector.class);

	private final Class<? extends ProcessingChannel.Agent> agentClass;

	private final String name;

	@Inject
	@Named("messageTypeMap")
	private Map<String, IMessageType> messageTypeMap;

	public AbstractMMInjector(Class<? extends ProcessingChannel.Agent> agentClass) {
		super(computeWorkDir(agentClass));

		this.agentClass = agentClass;
		this.name = agentClass.getSimpleName();
	}

	private static String computeWorkDir(Class<? extends ProcessingChannel.Agent> agentClass) {
		try {
			return VFS.getManager().getBaseFile().getURL().getPath() + File.separator + buildName(agentClass.getSimpleName());
		} catch (FileSystemException e) {
			throw new InjectorException("Exception when computing workdir for agent " + agentClass.getSimpleName(), e);
		}
	}

	public static String buildName(String agentName) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, agentName.replaceAll("Agent[^a-z]?", ""));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public final Object inject(FolderEventTriggerTask.FileTriggerEvent evt) {
		File file = (File) evt.getAttachment();
		if (evt instanceof FolderEventTriggerTask.NewFileTriggerEvent && file.getParentFile().equals(getWorkDir()) && !file.getName().endsWith(".tgz")) {
			try {
				String content;
				Path e = Paths.get(file.toURI());
				try (BufferedReader reader = Files.newBufferedReader(e, Charset.defaultCharset())) {
					content = dumpReader(reader);
				}

				injectMessage(StringUtils.trimToEmpty(content), file);
			} catch (Exception e) {
				LOG.error("INJECT ERROR", e);
			}
		}
		return null;
	}

	private String dumpReader(BufferedReader reader) throws IOException {
		StringBuilder content = new StringBuilder();
		for (String line; (line = reader.readLine()) != null; content.append(line)) {
			if (content.length() > 0) {
				content.append("\n");
			}
		}
		return content.toString();
	}

	@Override
	public final void injectMessage(String s, File file) {

		// we could check if the filename matches an ID to know if we should better recycle it

		IFSMessage message = createMessage();
		message.setMessageStatus(MessageStatus.TO_BE_INTEGRATED);
		IMessageType messageType = messageTypeMap.get(getName());
		message.setMessageType(messageType);
		message.setContent(s);
		message.setFile(file);

		injectLiseaMessage(message);
	}

	public final void injectLiseaMessage(IMessage message) {

		manageDeadlineDate(message);

		saveOrUpdateMessage(message);

		if (message instanceof IFSMessage) {
			File file = ((IFSMessage) message).getFile();
			if (file != null && !message.getId().toString().equals(file.getName())) {
				renameAndMoveFile((IFSMessage) message, file);
			}
		}

		if (message.getMessageStatus() != MessageStatus.REJECTED) {
			if (LOG.isDebugEnabled()) {
				IMessageType messageType = message.getMessageType();
				LOG.debug(String.format("[%s] Inject: %s", messageType != null ? messageType.getName() : "", message.getId()));
			}
			ProcessEngine.handle(getAgentName(), message);
		}
	}

	protected abstract IFSMessage createMessage();

	protected abstract void saveOrUpdateMessage(IMessage message);

	private void manageDeadlineDate(IMessage message) {
		if (message.getFirstProcessingDate() == null) {
			message.setFirstProcessingDate(Instant.now());
		}
		if (message.getMessageType() != null && message.getMessageType().getRecyclingDeadline() != null) {
			Instant deadlineDate = message.getFirstProcessingDate().plus(message.getMessageType().getRecyclingDeadline(), ChronoUnit.MINUTES);
			message.setDeadlineDate(deadlineDate);
		}
		// check deadline_date, only if it's not a new message
		if ((!(message instanceof IFSMessage) || ((IFSMessage) message).getFile() == null) && message.getDeadlineDate() != null && message.getDeadlineDate().isBefore(Instant.now())) {
			message.setMessageStatus(MessageStatus.REJECTED);
			LOG.warn(String.format("Message %s has reached its deadline, it has been rejected", message.getId()));
		}
	}

	private void renameAndMoveFile(IFSMessage message, File file) {
		// this can not happen if the message is being rejected: it would have no file
		File dest = new File(file.getParentFile() + File.separator + "accepted" + File.separator + message.getId().toString());
		boolean moved = file.renameTo(dest); //$NON-NLS-1$
		if (moved) {
			message.setFile(dest);
			message.setFolder("accepted"); //$NON-NLS-1$
			saveOrUpdateMessage(message);
		}
	}

	public final String getAgentName() {
		return agentClass.getSimpleName();
	}

	@Override
	public long getDelay() {
		return 2000;
	}
}
