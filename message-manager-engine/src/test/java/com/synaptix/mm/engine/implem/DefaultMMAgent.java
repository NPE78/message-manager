package com.synaptix.mm.engine.implem;

import com.google.inject.Inject;
import com.synaptix.component.factory.ComponentFactory;
import com.synaptix.mm.engine.AbstractMMAgent;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMMAgent extends AbstractMMAgent<DefaultProcessContext> {

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
	protected DefaultProcessContext buildProcessContext(Object messageObject) {
		DefaultProcessContext processContext = ComponentFactory.getInstance().createInstance(DefaultProcessContext.class);
		processContext.setMessage(new DefaultMessage(getMessageTypeName()));
		return processContext;
	}

	@Override
	protected void notifyMessageStatus(MessageStatus newMessageStatus) {
		getProcessContext().getMessage().setMessageStatus(newMessageStatus);
	}

	@Override
	protected boolean isConcerned(Object messageObject) {
		return true;
	}

	@Override
	protected void process(Object messageObject) {
		// do stuff
	}

	@Override
	protected void reject() {
		// do stuff
	}

	@Override
	protected void accept() {
		// do stuff
	}
}
