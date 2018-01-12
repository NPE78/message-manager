package com.talanlabs.mm.engine.unit;

import com.talanlabs.mm.engine.IMMProcess;
import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.MMEngine;
import com.talanlabs.mm.engine.factory.DefaultProcessErrorFactory;
import com.talanlabs.mm.engine.implem.DefaultMessage;
import com.talanlabs.mm.engine.model.DefaultErrorType;
import com.talanlabs.mm.engine.model.DefaultProcessError;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 30/10/2015.
 */
public class MMEngineTest {

	@Test
	public void testStart() {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();

		{
			dictionary.defineError(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		}

		engine.start(null, new MyProcess(), dictionary);
	}

	@Test
	public void testMMEngine() {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();

		dictionary.defineError(new DefaultErrorType("ACCEPT_WARN", ErrorRecyclingKind.WARNING));
		dictionary.defineError(new DefaultErrorType("REJECT_AUTO", ErrorRecyclingKind.AUTOMATIC));
		dictionary.defineError(new DefaultErrorType("REJECT_MAN", ErrorRecyclingKind.MANUAL));
		dictionary.defineError(new DefaultErrorType("REJECT_DEF", ErrorRecyclingKind.NOT_RECYCLABLE));

		MyMMProcess process = new MyMMProcess();
		{
			IProcessingResult processingResult = engine.start(new MyMessage("ACCEPT"), process, dictionary);
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertNull(processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start(new MyMessage("ACCEPT_WARN"), process, dictionary);
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start(new MyMessage("REJECT_AUTO"), process, dictionary);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, processingResult.getErrorRecyclingKind());
			Assert.assertNotNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start(new MyMessage("REJECT_MAN"), process, dictionary);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = engine.start(new MyMessage("REJECT_DEF"), process, dictionary);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
	}

	@Test
	public void testException() {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setProcessErrorFactory(new DefaultProcessErrorFactory());

		{
			dictionary.defineError(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		}

		IProcessingResult processingResult = engine.start(null, new MyExceptionProcess(), dictionary);

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

	private class MyMMProcess implements IMMProcess<MyMessage> {

		private List<IProcessError> processErrorList = new ArrayList<>();

		@Override
		public void process(MyMessage messageObject) {
			processErrorList.clear();

			String errorCode = messageObject.errorCode;
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

	private class MyProcess implements IMMProcess<MyMessage> {

		@Override
		public void process(MyMessage messageObject) {
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

	private class MyExceptionProcess implements IMMProcess<MyMessage> {

		private List<IProcessError> processErrorList = new ArrayList<>(); // should be threadlocal

		@Override
		public void process(MyMessage messageObject) {
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

	private class MyMessage extends DefaultMessage {

        private final String errorCode;

        public MyMessage(String errorCode) {
            super("messageType");

            this.errorCode = errorCode;
        }
    }
}
