package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

import junit.framework.Assert;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class ITProcessingResult extends AbstractMMTest {

	@Test
	public void testRecycling() throws Exception {
		MMDictionary dictionary = getInstance(MMDictionary.class);

		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("MT1"));
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.AUTOMATIC));
			errorTypeList.add(new DefaultErrorType("ET2", ErrorRecyclingKind.MANUAL));
		}
		{
			List<IErrorType> errorTypeList = new ArrayList<>();
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.WARNING));
			dictionary.addMessageType(new DefaultMessageType("MT2"), errorTypeList);
		}
		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("MT3"));
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.MANUAL));
			errorTypeList.add(new DefaultErrorType("ET2", ErrorRecyclingKind.WARNING));
			errorTypeList.add(new DefaultErrorType("ET3", ErrorRecyclingKind.NOT_RECYCLABLE));
		}
		{
			boolean raised = false;
			try {
				dictionary.addMessageType(new DefaultMessageType("MT1"), new ArrayList<>());
			} catch (Exception e) {
				raised = true;
			}
			Assert.assertTrue(raised); // test unique
		}

		IProcessErrorFactory processErrorFactory = getInstance(IProcessErrorFactory.class);

		List<IProcessError> errorList = new ArrayList<>();

		{
			IProcessingResult r1 = dictionary.getProcessingResult("MT1", errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
		}
		{
			errorList.add(processErrorFactory.createProcessError("ET1", "", ""));
			IProcessingResult r1 = dictionary.getProcessingResult("MT1", errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, r1.getErrorRecyclingKind());
			Assert.assertNotNull(r1.getNextProcessingDate());
		}
		{
			IProcessingResult r1 = dictionary.getProcessingResult("MT2", errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			errorList.add(processErrorFactory.createProcessError("ET2", "", ""));
			IProcessingResult r1 = dictionary.getProcessingResult("MT3", errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			errorList.add(processErrorFactory.createProcessError("ET3", "", ""));
			IProcessingResult r1 = dictionary.getProcessingResult("MT3", errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			boolean exceptionRaised = false;
			try {
				dictionary.getProcessingResult("MT1", errorList);
			} catch (UnknownErrorException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}
	}
}
