package com.synaptix.mm.engine.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.IMMProcess;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.MMEngine;
import com.synaptix.mm.engine.SubDictionary;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 30/10/2015.
 */
public class MMEngineTest {

	@Test
	public void testStart() throws Exception {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setDictionary(dictionary);

		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("test"));
			errorTypeList.add(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		}

		engine.start(null, new MyProcess());
	}

	@Test
	public void testMMEngine() throws Exception {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setDictionary(dictionary);

		List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("TEST"));
		errorTypeList.add(new DefaultErrorType("ACCEPT_WARN", ErrorRecyclingKind.WARNING));
		errorTypeList.add(new DefaultErrorType("REJECT_AUTO", ErrorRecyclingKind.AUTOMATIC));
		errorTypeList.add(new DefaultErrorType("REJECT_MAN", ErrorRecyclingKind.MANUAL));
		errorTypeList.add(new DefaultErrorType("REJECT_DEF", ErrorRecyclingKind.NOT_RECYCLABLE));

		MyMMProcess process = new MyMMProcess();
		{
			IProcessingResult processingResult = engine.start("ACCEPT", process);
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertNull(processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start("ACCEPT_WARN", process);
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start("REJECT_AUTO", process);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, processingResult.getErrorRecyclingKind());
			Assert.assertNotNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start("REJECT_MAN", process);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start("REJECT_DEF", process);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
	}

	private class MyMMProcess implements IMMProcess {

		private List<IProcessError> processErrorList = new ArrayList<>();

		@Override
		public void process(Object messageObject) {
			processErrorList.clear();

			String errorCode = (String) messageObject;
			if (!"ACCEPT".equals(errorCode)) {
				processErrorList.add(new DefaultProcessError(errorCode, "", ""));
			}
		}

		@Override
		public void reject() {
		}

		@Override
		public void accept() {
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus) {
		}

		@Override
		public List<IProcessError> getProcessErrorList() {
			return processErrorList;
		}

		@Override
		public String getMessageTypeName() {
			return "TEST";
		}

		@Override
		public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
			return rootDictionary;
		}
	}

	private class MyProcess implements IMMProcess {

		@Override
		public void process(Object messageObject) {
		}

		@Override
		public void reject() {
			Assert.assertTrue(true);
		}

		@Override
		public void accept() {
			Assert.assertTrue(false);
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus) {
		}

		@Override
		public List<IProcessError> getProcessErrorList() {
			DefaultProcessError error = new DefaultProcessError("AUTO", "", "");
			return Arrays.asList(error);
		}

		@Override
		public String getMessageTypeName() {
			return "test";
		}

		@Override
		public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
			return rootDictionary;
		}
	}
}
