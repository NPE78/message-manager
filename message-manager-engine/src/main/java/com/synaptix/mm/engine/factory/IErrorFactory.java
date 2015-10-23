package com.synaptix.mm.engine.factory;

import com.synaptix.mm.shared.model.IError;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IErrorFactory {

	public IError createError(String messageTypeName, String errorCode);

}
