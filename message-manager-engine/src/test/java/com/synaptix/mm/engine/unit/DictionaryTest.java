package com.synaptix.mm.engine.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.SubDictionary;
import com.synaptix.mm.engine.exception.InvalidDictionaryOperationException;
import com.synaptix.mm.engine.exception.UnknownDictionaryException;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultProcessError;
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
		SubDictionary mt1Dictionary = dictionary.addSubsetDictionary("MT1");
		SubDictionary mt2Dictionary = dictionary.addSubsetDictionary("MT2");
		SubDictionary mt3Dictionary = dictionary.addSubsetDictionary("MT3");

		DefaultErrorType mt1et1 = new DefaultErrorType("ET1", ErrorRecyclingKind.AUTOMATIC);
		{
			mt1Dictionary.defineError(mt1et1);
			mt1Dictionary.defineError(new DefaultErrorType("ET2", ErrorRecyclingKind.MANUAL));

			mt1Dictionary.defineError(new DefaultErrorType("ET2", ErrorRecyclingKind.WARNING, 60));
			mt1Dictionary.defineError(new DefaultErrorType("ET2", ErrorRecyclingKind.AUTOMATIC, 60));

			mt1Dictionary.addSubsetDictionary("sub.ter");
		}
		{
			mt2Dictionary.defineError(new DefaultErrorType("ET1", ErrorRecyclingKind.WARNING));
		}
		{
			mt3Dictionary.defineError(new DefaultErrorType("ET1", ErrorRecyclingKind.MANUAL));
			mt3Dictionary.defineError(new DefaultErrorType("ET2", ErrorRecyclingKind.WARNING));
			mt3Dictionary.defineError(new DefaultErrorType("ET3", ErrorRecyclingKind.NOT_RECYCLABLE));
		}

		DefaultProcessErrorFactory processErrorFactory = new DefaultProcessErrorFactory();

		List<IProcessError> errorList = new ArrayList<>();

		{
			IProcessingResult r1 = mt1Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
		}
		{
			DefaultProcessError et1 = processErrorFactory.createProcessError("ET1");
			et1.setAttribute("");
			et1.setValue("");
			errorList.add(et1);
			IProcessingResult r1 = mt1Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, r1.getErrorRecyclingKind());
			Assert.assertNotNull(r1.getNextProcessingDate());
		}
		{
			IProcessingResult r1 = mt1Dictionary.getSubsetDictionary("sub").getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, r1.getErrorRecyclingKind());
			Assert.assertNotNull(r1.getNextProcessingDate());
		}
		{
			IProcessingResult r1 = mt2Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			DefaultProcessError et2 = processErrorFactory.createProcessError("ET2");
			et2.setAttribute("");
			et2.setValue("");
			errorList.add(et2);
			IProcessingResult r1 = mt3Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			IProcessingResult r1 = mt1Dictionary.getSubsetDictionary("sub").getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, r1.getErrorRecyclingKind());
			Assert.assertNotNull(r1.getNextProcessingDate());
		}
		{
			DefaultProcessError et3 = processErrorFactory.createProcessError("ET3");
			et3.setAttribute("");
			et3.setValue("");
			errorList.add(et3);
			IProcessingResult r1 = mt3Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.INVALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}
		{
			boolean exceptionRaised = false;
			IProcessingResult processingResult = mt1Dictionary.getProcessingResult(errorList);
			if (processingResult.getException() instanceof UnknownErrorException) {
				exceptionRaised = true;
			}
			Assert.assertTrue(exceptionRaised);
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
			DefaultProcessError unknownError = processErrorFactory.createProcessError("unknownError");
			unknownError.setAttribute("");
			unknownError.setValue("");
			errorList.add(unknownError);
			IProcessingResult processingResult = mt1Dictionary.getSubsetDictionary("sub").getProcessingResult(errorList);
			if (processingResult.getException() instanceof UnknownErrorException) {
				exceptionRaised = true;
			}
			Assert.assertTrue(exceptionRaised);
		}

		{
			boolean exceptionRaised = false;
			try {
				dictionary.addSubsetDictionary("test.sub.");
			} catch (InvalidDictionaryOperationException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}

		{
			boolean exceptionRaised = false;
			try {
				dictionary.addSubsetDictionary(".test.sub");
			} catch (InvalidDictionaryOperationException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}

		Assert.assertTrue(dictionary.getSubsetDictionary("MT1.sub.ter").destroy());

		{
			boolean exceptionRaised = false;
			try {
				dictionary.destroy();
			} catch (InvalidDictionaryOperationException e) {
				exceptionRaised = true;
			} finally {
				Assert.assertTrue(exceptionRaised);
			}
		}

		Map<String, IErrorType> errorMap = dictionary.getErrorMap();
		Assert.assertEquals(mt1et1, errorMap.get("MT1.ET1"));
		Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, errorMap.get("MT1.ET1").getRecyclingKind());

		dictionary.clear();
		mt1Dictionary = dictionary.addSubsetDictionary("MT1");
		errorList.clear();
		mt1et1 = new DefaultErrorType("ET1", ErrorRecyclingKind.WARNING);
		{
			mt1Dictionary.defineError(mt1et1);
			mt1Dictionary.defineError(new DefaultErrorType("ET2", ErrorRecyclingKind.MANUAL));
		}
		{
			IProcessingResult r1 = mt1Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
		}
		{
			DefaultProcessError et1 = processErrorFactory.createProcessError("ET1");
			et1.setAttribute("");
			et1.setValue("");
			errorList.add(et1);
			IProcessingResult r1 = mt1Dictionary.getProcessingResult(errorList);
			Assert.assertEquals(IProcessingResult.State.VALID, r1.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, r1.getErrorRecyclingKind());
			Assert.assertNull(r1.getNextProcessingDate());
		}

		{
			boolean errorRaised = false;
			SubDictionary dictionary1 = dictionary.addSubsetDictionary("SUB.TER");

			try {
				dictionary.addSubsetDictionary("SUB.TER");
			} catch (InvalidDictionaryOperationException e) {
				errorRaised = true;
			}
			Assert.assertTrue(errorRaised);

			SubDictionary dictionary2 = dictionary.getOrCreateSubsetDictionary("SUB.TER");
			Assert.assertEquals(dictionary1, dictionary2);
		}

		{
			boolean errorRaised = false;
			try {
				dictionary.getSubsetDictionary(" ");
			} catch (UnknownDictionaryException e) {
				errorRaised = true;
			}
			Assert.assertTrue(errorRaised);
		}
		{
			boolean errorRaised = false;
			try {
				dictionary.getOrCreateSubsetDictionary("test dic");
			} catch (InvalidDictionaryOperationException e) {
				errorRaised = true;
			}
			Assert.assertTrue(errorRaised);
		}
		{
			boolean errorRaised = false;
			try {
				dictionary.getOrCreateSubsetDictionary(" ");
			} catch (InvalidDictionaryOperationException e) {
				errorRaised = true;
			}
			Assert.assertTrue(errorRaised);
		}
		{
			boolean errorRaised = false;
			try {
				dictionary.getOrCreateSubsetDictionary("MAIN");
			} catch (InvalidDictionaryOperationException e) {
				errorRaised = true;
			}
			Assert.assertTrue(errorRaised);
		}

		errorMap = dictionary.getErrorMap();
		Assert.assertEquals(mt1et1, errorMap.get("MT1.ET1"));
		Assert.assertEquals(ErrorRecyclingKind.WARNING, errorMap.get("MT1.ET1").getRecyclingKind());
	}
}
