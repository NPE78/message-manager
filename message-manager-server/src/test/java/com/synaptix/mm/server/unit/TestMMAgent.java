package com.synaptix.mm.server.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.google.inject.Inject;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.server.implem.DefaultMMAgent;
import com.synaptix.mm.server.it.AbstractMMTest;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.MessageWay;

import junit.framework.Assert;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class TestMMAgent extends AbstractMMTest {

	@Test
	public void testMMAgent() throws Exception {

		LOG.info("Starting TestMMAgent");

		MMDictionary dictionary = getInstance(MMDictionary.class);

		List<TestCase> testCaseList = new ArrayList<>();
		List<IProcessError> processErrorList = new ArrayList<>();
		{
			processErrorList.add(new DefaultProcessError("WARN", "Warning error", "Test"));
			testCaseList.add(new TestCase("Warning case", processErrorList, IProcessingResult.State.VALID, ErrorRecyclingKind.WARNING));
		}
		{
			processErrorList.add(new DefaultProcessError("AUTO", "Automatic error", "Test"));
			testCaseList.add(new TestCase("Automatic case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.AUTOMATIC));
		}
		{
			processErrorList.add(new DefaultProcessError("MAN", "Manual error", "Test"));
			testCaseList.add(new TestCase("Manual case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.MANUAL));
		}
		{
			processErrorList.add(new DefaultProcessError("REJ", "Rejected error", "Test"));
			testCaseList.add(new TestCase("Rejected case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.NOT_RECYCLABLE));
		}


		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("TEST_AGENT_IMPORT"));
			errorTypeList.add(new DefaultErrorType("WARN", ErrorRecyclingKind.WARNING));
			errorTypeList.add(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
			errorTypeList.add(new DefaultErrorType("MAN", ErrorRecyclingKind.MANUAL));
			errorTypeList.add(new DefaultErrorType("REJ", ErrorRecyclingKind.NOT_RECYCLABLE));
		}
		{
			List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("TEST_AGENT_EXPORT", MessageWay.OUT));
			errorTypeList.add(new DefaultErrorType("WARN", ErrorRecyclingKind.WARNING));
			errorTypeList.add(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
			errorTypeList.add(new DefaultErrorType("MAN", ErrorRecyclingKind.MANUAL));
			errorTypeList.add(new DefaultErrorType("REJ", ErrorRecyclingKind.NOT_RECYCLABLE));
		}

		testCaseList.forEach(testCase -> {
					LOG.info("Testing " + testCase.name);
					IProcessingResult processingResult = getInstance(MyImportAgent.class).doWork(testCase);
					Assert.assertTrue(testCase.name + ", got " + processingResult.getState() + ", " + processingResult.getErrorRecyclingKind(), testCase.isTestValid(processingResult));
				}
		);

		{ // test unknown exception
			processErrorList.add(new DefaultProcessError("UNKNOWN", "Unknown error", "Test"));
			TestCase testCase = new TestCase("Unknown error case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.MANUAL);
			boolean raised = false;
			try {
				LOG.info("Testing " + testCase.name);
				getInstance(MyImportAgent.class).doWork(testCase);
			} catch (UnknownErrorException e) {
				raised = true;
			}
			Assert.assertTrue(raised);
		}

		{ // test export
			processErrorList.clear();
			TestCase testCase = new TestCase("Export error case", processErrorList, IProcessingResult.State.VALID, null);
			LOG.info("Testing " + testCase.name);
			IProcessingResult processingResult = getInstance(MyExportAgent.class).doWork(testCase);
			Assert.assertTrue(testCase.name + ", got " + processingResult.getState() + ", " + processingResult.getErrorRecyclingKind(), testCase.isTestValid(processingResult));
		}

		{ // test foreign
			IProcessingResult processingResult = getInstance(MyForeignAgent.class).doWork(null);
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
		}

		{ // test bug
			MyObject obj = new MyObject() {
				boolean ok;
			};
			try {
				getInstance(MyBugguedAgent.class).work(obj, null);
			} catch (Exception e) {
			}
			Assert.assertTrue(obj.ok);
		}
	}

	private class MyObject {

		boolean ok;

	}

	private static class MyImportAgent extends DefaultMMAgent {

		@Inject
		private MyImportAgent() {
			super("TEST_AGENT_IMPORT");
		}

		@Override
		public void process(Object messageObject) {
			TestCase testCase = (TestCase) messageObject;
			List<IProcessError> processErrorList = testCase.getErrors();

			processErrorList.forEach(processError -> addError(processError.getErrorCode(), processError.getAttribute(), processError.getValue()));
		}

		@Override
		protected boolean isConcerned(Object messageObject) {
			return messageObject instanceof TestCase;
		}
	}

	private static class MyExportAgent extends DefaultMMAgent {

		@Inject
		private MyExportAgent() {
			super("TEST_AGENT_EXPORT");
		}

		@Override
		public void process(Object messageObject) {
			TestCase testCase = (TestCase) messageObject;
			List<IProcessError> processErrorList = testCase.getErrors();

			processErrorList.forEach(processError -> addError(processError.getErrorCode(), processError.getAttribute(), processError.getValue()));
		}

		@Override
		protected boolean isConcerned(Object messageObject) {
			return messageObject instanceof TestCase;
		}
	}

	private static class MyForeignAgent extends DefaultMMAgent {

		@Inject
		private MyForeignAgent() {
			super("TEST_AGENT_FOREIGN");
		}

		@Override
		public void process(Object messageObject) {
		}

		@Override
		protected boolean isConcerned(Object messageObject) {
			return false;
		}
	}

	private static class MyBugguedAgent extends DefaultMMAgent {

		private MyObject obj;

		@Inject
		private MyBugguedAgent() {
			super("TEST_AGENT_BUG");
		}

		@Override
		public void process(Object messageObject) {
			this.obj = (MyObject) messageObject;
			throw new RuntimeException("error");
		}

		@Override
		public void reject() {
			obj.ok = true;
		}
	}

	private class TestCase {

		private final String name;

		private final List<IProcessError> processErrorList;

		private final IProcessingResult.State expectedState;

		private final ErrorRecyclingKind expectedErrorRecyclingKind;

		private TestCase(String name, List<IProcessError> processErrorList, IProcessingResult.State expectedState, ErrorRecyclingKind expectedErrorRecyclingKind) {
			this.name = name;
			this.processErrorList = Collections.unmodifiableList(new ArrayList<>(processErrorList));
			this.expectedState = expectedState;
			this.expectedErrorRecyclingKind = expectedErrorRecyclingKind;
		}

		public List<IProcessError> getErrors() {
			return processErrorList;
		}

		public boolean isTestValid(IProcessingResult processingResult) {
			return Objects.equals(expectedState, processingResult.getState()) && Objects.equals(expectedErrorRecyclingKind, processingResult.getErrorRecyclingKind());
		}
	}
}