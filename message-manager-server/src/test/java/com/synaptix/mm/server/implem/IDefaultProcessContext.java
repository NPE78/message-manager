package com.synaptix.mm.server.implem;

import com.synaptix.mm.engine.implem.DefaultMessage;
import com.synaptix.mm.server.model.IProcessContext;

/**
 * Created by NicolasP on 26/10/2015.
 */
public interface IDefaultProcessContext extends IProcessContext {

	DefaultMessage getMessage();

	void setMessage(DefaultMessage defaultMessage);

}
