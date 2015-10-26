package com.synaptix.mm.engine.implem;

import com.google.inject.Inject;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DefaultProcessErrorFactory implements IProcessErrorFactory {

	@Inject
	private MMDictionary dictionary;

	@Override
	public IProcessError createProcessError(String errorCode, String attribute, String value) {
		return new DefaultProcessError(errorCode, attribute, value);
	}
}
