package com.talanlabs.mm.engine.factory;

import com.talanlabs.mm.engine.model.DefaultProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultProcessErrorFactory implements IProcessErrorFactory {

	@Override
	public DefaultProcessError createProcessError(String errorCode) {
		return new DefaultProcessError(errorCode);
	}
}
