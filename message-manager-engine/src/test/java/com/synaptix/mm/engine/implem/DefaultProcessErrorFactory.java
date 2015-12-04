package com.synaptix.mm.engine.implem;

import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultProcessErrorFactory implements IProcessErrorFactory {

	@Override
	public IProcessError createProcessError(String errorCode, String attribute, String value) {
		return new DefaultProcessError(errorCode, attribute, value);
	}
}
