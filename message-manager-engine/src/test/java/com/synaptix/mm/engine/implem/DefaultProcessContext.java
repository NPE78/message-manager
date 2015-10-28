package com.synaptix.mm.engine.implem;

import com.synaptix.mm.engine.model.IProcessContext;

/**
 * Created by NicolasP on 26/10/2015.
 */
public interface DefaultProcessContext extends IProcessContext {

	DefaultMessage getMessage();

	void setMessage(DefaultMessage defaultMessage);

}
