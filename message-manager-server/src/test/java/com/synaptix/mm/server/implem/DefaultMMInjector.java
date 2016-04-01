package com.synaptix.mm.server.implem;

import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.server.injector.AbstractMMInjector;
import com.synaptix.mm.shared.model.IFSMessage;
import com.synaptix.mm.shared.model.IMessage;

/**
 * Created by NicolasP on 01/04/2016.
 */
public class DefaultMMInjector extends AbstractMMInjector {

	private IFSMessage lastMessage;

	public DefaultMMInjector() {
		super(DefaultMMAgent.class);
	}

	@Override
	protected IFSMessage createMessage() {
		lastMessage = new DefaultMessage("DEFAULT");
		return lastMessage;
	}

	@Override
	protected void saveOrUpdateMessage(IMessage message) {
	}

	public IFSMessage getLastMessage() {
		return lastMessage;
	}
}
