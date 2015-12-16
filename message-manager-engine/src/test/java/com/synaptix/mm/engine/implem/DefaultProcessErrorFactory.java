package com.synaptix.mm.engine.implem;

import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultProcessErrorFactory implements IProcessErrorFactory {

	@Override
	public DefaultProcessError createProcessError(String errorCode) {
		return new DefaultProcessError(errorCode);
	}
}
