package com.synaptix.mm.engine.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.SubDictionary;
import com.synaptix.mm.engine.exception.UnknownDictionaryException;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.exception.UnknownMessageTypeException;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public class DictionaryTest {

	@Test
	public void testRecycling() throws Exception {
		MMDictionary dictionary = new MMDictionary();
		SubDictionary subDictionary = dictionary.addSubsetDictionary("sub");
		SubDictionary terDictionary = subDictionary.addSubsetDictionary("ter");

		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("MT1"));
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.AUTOMATIC));
			errorTypeList.add(new DefaultErrorType("ET2", ErrorRecyclingKind.MANUAL));

			subDictionary.fixError("MT1", new DefaultErrorType("ET2", ErrorRecyclingKind.WARNING, 60));
			subDictionary.fixError("MT1", new DefaultErrorType("ET2", ErrorRecyclingKind.AUTOMATIC, 60));

			Assert.assertNotNull(dictionary.getMessageType("MT1"));
		}
		{
			List<IErrorType> errorTypeList = new ArrayList<>();
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.WARNING));
			dictionary.addMessageType(new DefaultMessageType("MT2"), errorTypeList);

			Assert.assertNotNull(dictionary.getMessageType("MT2"));
		}
		{
			dictionary.addMessageType(new DefaultMessageType("MT3"), null);
			dictionary.fixError("MT3",new DefaultErrorType("ET1", ErrorRecyclingKind.MANUAL));
			dictionary.fixError("MT3",new DefaultErrorType("ET2", ErrorRecyclingKind.WARNING));
			dictionary.fixError("MT3",new DefaultErrorType("ET3", ErrorRecyclingKind.NOT_RECYCLABLE));

			Assert.assertNotNull(dictionary.getMessageType("MT3"));
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

		IProcessErrorFactory processErrorFactory = new DefaultProcessErrorFactory();

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
			IProcessingResult r1 = dictionary.getSubsetDictionary("sub").getProcessingResult("MT1", errorList);
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
			IProcessingResult r1 = dictionary.getSubsetDictionary("sub").getProcessingResult("MT1", errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, r1.getErrorRecyclingKind());
			Assert.assertNotNull(r1.getNextProcessingDate());
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
		{
			boolean exceptionRaised = false;
			try {
				dictionary.getSubsetDictionary("unknownSubDictionary");
			} catch (UnknownDictionaryException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}
		{
			boolean exceptionRaised = false;
			try {
				dictionary.getProcessingResult("unknownMessageType", errorList);
			} catch (UnknownMessageTypeException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}
		{
			boolean exceptionRaised = false;
			try {
				dictionary.getMessageType("unknownMessageType");
			} catch (UnknownMessageTypeException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}
		{
			boolean exceptionRaised = false;
			try {
				errorList.add(processErrorFactory.createProcessError("unknownError", "", ""));
				dictionary.getSubsetDictionary("sub.ter").getProcessingResult("MT3", errorList);
			} catch (UnknownErrorException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}

		dictionary.clear();
		errorList.clear();
		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("MT1"));
			errorTypeList.add(new DefaultErrorType("ET1", ErrorRecyclingKind.WARNING));
			errorTypeList.add(new DefaultErrorType("ET2", ErrorRecyclingKind.MANUAL));

			Assert.assertNotNull(dictionary.getMessageType("MT1"));
		}
		{
			IProcessingResult r1 = dictionary.getProcessingResult("MT1", errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
		}
		{
			errorList.add(processErrorFactory.createProcessError("ET1", "", ""));
			IProcessingResult r1 = dictionary.getProcessingResult("MT1", errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
	}
}
