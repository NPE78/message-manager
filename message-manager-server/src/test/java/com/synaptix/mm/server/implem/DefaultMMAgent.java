package com.synaptix.mm.server.implem;

import java.util.Map;

import com.google.inject.Inject;
import com.synaptix.component.factory.ComponentFactory;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.SubDictionary;
import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.server.AbstractMMAgent;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMMAgent extends AbstractMMAgent<IDefaultProcessContext> {

	@Inject
	public DefaultMMAgent() {
		super("DEFAULT");
	}

	/**
	 * For tests only
	 */
	public DefaultMMAgent(String messageTypeName) {
		super(messageTypeName);
	}

	@Override
	protected IDefaultProcessContext buildProcessContext(Object messageObject) {
		IDefaultProcessContext processContext = ComponentFactory.getInstance().createInstance(IDefaultProcessContext.class);
		processContext.setMessage(new DefaultMessage(getMessageTypeName()));
		return processContext;
	}

	@Override
	public void notifyMessageStatus(MessageStatus newMessageStatus) {
		getProcessContext().getMessage().setMessageStatus(newMessageStatus);
	}

	@Override
	protected boolean isConcerned(Object messageObject) {
		return true;
	}

	@Override
	public void process(Object messageObject) {
		// do stuff
	}

	@Override
	public void reject(Map<IProcessError, ErrorImpact> errorMap) {
		// do stuff
	}

	@Override
	public void accept(Map<IProcessError, ErrorImpact> errorMap) {
		// do stuff
	}

	@Override
	public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
		return rootDictionary;
	}

	@Override
	public MessageWay getMessageWay() {
		return MessageWay.IN;
	}
}
