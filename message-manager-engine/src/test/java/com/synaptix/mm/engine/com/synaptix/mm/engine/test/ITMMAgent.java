package com.synaptix.mm.engine.com.synaptix.mm.engine.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.google.inject.Inject;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.implem.DefaultMMAgent;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageInType;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class ITMMAgent extends AbstractMMTest {

	@Test
	public void testMMAgent() throws Exception {

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


		List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageInType("TEST_AGENT"));
		errorTypeList.add(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		errorTypeList.add(new DefaultErrorType("MAN", ErrorRecyclingKind.MANUAL));
		errorTypeList.add(new DefaultErrorType("WARN", ErrorRecyclingKind.WARNING));
		errorTypeList.add(new DefaultErrorType("REJ", ErrorRecyclingKind.NOT_RECYCLABLE));

		testCaseList.forEach(testCase -> {
					IProcessingResult processingResult = getInstance(MyAgent.class).doWork(testCase);
					Assert.assertTrue(testCase.name + ", got " + processingResult.getState() + ", " + processingResult.getErrorRecyclingKind(), testCase.isTestValid(processingResult));
				}
		);

		{
			processErrorList.add(new DefaultProcessError("UKNOWN", "Unknown error", "Test"));
			TestCase testCase = new TestCase("Unknown error case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.MANUAL);
			boolean raised = false;
			try {
				IProcessingResult processingResult = getInstance(MyAgent.class).doWork(testCase);
			} catch (UnknownErrorException e) {
				raised = true;
			}
			Assert.assertTrue(raised);
		}
	}

	@Override
	AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new DefaultTestMMModule() {

			@Override
			protected void configureTestModule() {
				bindAgent(MyAgent.class, 5, 10);
			}
		};
	}

	private static class MyAgent extends DefaultMMAgent {

		@Inject
		public MyAgent() {
			super("TEST_AGENT");
		}

		@Override
		protected void process(Object messageObject) {
			TestCase testCase = (TestCase) messageObject;
			List<IProcessError> processErrorList = testCase.getErrors();

			processErrorList.forEach(processError -> addError(processError.getErrorCode(), processError.getAttribute(), processError.getValue()));
		}

		@Override
		protected boolean isConcerned(Object messageObject) {
			return messageObject instanceof TestCase ? true : false;
		}
	}

	private class TestCase {

		private final String name;

		private final List<IProcessError> processErrorList;

		private final IProcessingResult.State expectedState;

		private final ErrorRecyclingKind expectedErrorRecyclingKind;

		public TestCase(String name, List<IProcessError> processErrorList, IProcessingResult.State expectedState, ErrorRecyclingKind expectedErrorRecyclingKind) {
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
