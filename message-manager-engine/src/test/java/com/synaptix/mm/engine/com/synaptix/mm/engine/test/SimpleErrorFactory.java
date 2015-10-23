//package com.synaptix.mm.engine.com.synaptix.mm.engine.test;
//
//import java.util.Calendar;
//
//import com.google.inject.Inject;
//import com.synaptix.mm.engine.MMDictionary;
//import com.synaptix.mm.engine.factory.IErrorFactory;
//import com.synaptix.mm.engine.model.SimpleError;
//import com.synaptix.mm.shared.model.IError;
//import com.synaptix.mm.shared.model.IErrorType;
//import com.synaptix.mm.shared.model.domain.ErrorStatus;
//
///**
// * Created by NicolasP on 22/10/2015.
// */
//public class SimpleErrorFactory implements IErrorFactory {
//
//	@Inject
//	private MMDictionary dictionary;
//
//	@Override
//	public IError createError(String messageTypeName, String errorCode) {
//		IErrorType errorType = dictionary.findErrorType(messageTypeName, errorCode);
//
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.MINUTE, errorType.getNextRecyclingDuration());
//
//		return new SimpleError(errorType, errorType.getRecyclingKind(), ErrorStatus.OPENED, cal.getTime());
//	}
//}
