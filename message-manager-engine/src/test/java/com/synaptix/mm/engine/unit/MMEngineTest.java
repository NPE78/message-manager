package com.synaptix.mm.engine.unit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.synaptix.mm.engine.IMMProcess;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.MMEngine;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;

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
			dictionary.defineError(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		}

		engine.start(null, new MyProcess());
	}

	@Test
	public void testMMEngine() throws Exception {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setDictionary(dictionary);

		dictionary.defineError(new DefaultErrorType("ACCEPT_WARN", ErrorRecyclingKind.WARNING));
		dictionary.defineError(new DefaultErrorType("REJECT_AUTO", ErrorRecyclingKind.AUTOMATIC));
		dictionary.defineError(new DefaultErrorType("REJECT_MAN", ErrorRecyclingKind.MANUAL));
		dictionary.defineError(new DefaultErrorType("REJECT_DEF", ErrorRecyclingKind.NOT_RECYCLABLE));

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

	@Test
	public void testException() throws Exception {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setDictionary(dictionary);
		engine.setProcessErrorFactory(new DefaultProcessErrorFactory());

		{
			dictionary.defineError(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		}

		IProcessingResult processingResult = engine.start(null, new MyExceptionProcess());

		Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
		boolean hasUnknownError = false;
		for (Map.Entry<IProcessError, ErrorImpact> entry : processingResult.getErrorMap().entrySet()) {
			if ("UNKNOWN_ERROR".equals(entry.getKey().getErrorCode())) {
				hasUnknownError = true;
				Assert.assertEquals(ErrorRecyclingKind.MANUAL, entry.getValue().getRecyclingKind());
			}
		}
		Assert.assertTrue(hasUnknownError);
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
		public void reject(Map<IProcessError, ErrorImpact> errorMap) {
		}

		@Override
		public void accept(Map<IProcessError, ErrorImpact> errorMap) {
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
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
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}
	}

	private class MyProcess implements IMMProcess {

		@Override
		public void process(Object messageObject) {
		}

		@Override
		public void reject(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertTrue(true);
		}

		@Override
		public void accept(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertTrue(false);
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
		}

		@Override
		public List<IProcessError> getProcessErrorList() {
			DefaultProcessError error = new DefaultProcessError("AUTO", "", "");
			return Collections.singletonList(error);
		}

		@Override
		public String getMessageTypeName() {
			return "test";
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}
	}

	private class MyExceptionProcess implements IMMProcess {

		private List<IProcessError> processErrorList = new ArrayList<>(); // should be threadlocal

		@Override
		public void process(Object messageObject) {
			throw new RuntimeException("exception test");
		}

		@Override
		public void reject(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertTrue(true);
		}

		@Override
		public void accept(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertTrue(false);
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
		}

		@Override
		public List<IProcessError> getProcessErrorList() {
			return processErrorList;
		}

		@Override
		public String getMessageTypeName() {
			return "exception";
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}
	}
}
