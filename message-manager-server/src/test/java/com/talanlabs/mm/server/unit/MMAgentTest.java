package com.talanlabs.mm.server.unit;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.engine.exception.UnknownDictionaryException;
import com.talanlabs.mm.engine.exception.UnknownErrorException;
import com.talanlabs.mm.engine.model.DefaultErrorType;
import com.talanlabs.mm.engine.model.DefaultProcessError;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.server.AbstractMMAgent;
import com.talanlabs.mm.server.AbstractMMTest;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.implem.DefaultMMAgent;
import com.talanlabs.mm.server.model.AbstractMMFlux;
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
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by NicolasP on 28/10/2015.
 */
public class MMAgentTest extends AbstractMMTest {

    private MyMMAgent myMMAgent;
    private MyImportAgent myImportAgent;
    private MyExportAgent myExportAgent;
    private MyForeignAgent myForeignAgent;
    private MyBugguedAgent myBugguedAgent;

    @Before
    public void prepareAgent() {
        myMMAgent = new MyMMAgent();
        myMMAgent.register("test", 5);

        myImportAgent = new MyImportAgent();
        myImportAgent.register("test", 5);

        myExportAgent = new MyExportAgent();
        myExportAgent.register("test", 5);

        myForeignAgent = new MyForeignAgent();
        myForeignAgent.register("test", 5);

        myBugguedAgent = new MyBugguedAgent();
        myBugguedAgent.register("test", 5);

        startIntegrator();
    }

	@Test
    // in this test, we put an error in a agent process to see how it reacts
	public void testErrorManagement() {

        MMDictionary dictionary = MMEngineAddon.getDictionary("test");

        dictionary.defineError(new DefaultErrorType("ACCEPT_WARN", ErrorRecyclingKind.WARNING));
		dictionary.defineError(new DefaultErrorType("REJECT_AUTO", ErrorRecyclingKind.AUTOMATIC));
		dictionary.defineError(new DefaultErrorType("REJECT_MAN", ErrorRecyclingKind.MANUAL));
		dictionary.defineError(new DefaultErrorType("REJECT_DEF", ErrorRecyclingKind.NOT_RECYCLABLE));

		{
			IProcessingResult processingResult = myMMAgent.dispatchMessage(new MyFlux("ACCEPT"), "test");
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertNull(processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = myMMAgent.dispatchMessage(new MyFlux("ACCEPT_WARN"), "test");
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = myMMAgent.dispatchMessage(new MyFlux("REJECT_AUTO"), "test");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, processingResult.getErrorRecyclingKind());
			Assert.assertNotNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = myMMAgent.dispatchMessage(new MyFlux("REJECT_MAN"), "test");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = myMMAgent.dispatchMessage(new MyFlux("REJECT_DEF"), "test");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
	}

	@Test
	public void testMMAgent2() throws Exception {
        MMDictionary dictionary = MMEngineAddon.getDictionary("test");

		List<TestCase> testCaseList = new ArrayList<>();
		List<IProcessError> processErrorList = new ArrayList<>();
		{
			processErrorList.add(new DefaultProcessError("WARN", "Warning error", "Test"));
			testCaseList.add(new TestCase("Warning case", processErrorList, IProcessingResult.State.VALID, ErrorRecyclingKind.WARNING));
		}
		{
			processErrorList.add(new DefaultProcessError("TER", "Automatic error", "Test"));
			testCaseList.add(new TestCase("Automatic from sub.ter case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.AUTOMATIC));
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

		dictionary.defineError(new DefaultErrorType("WARN", ErrorRecyclingKind.WARNING));
		dictionary.defineError(new DefaultErrorType("AUTO", ErrorRecyclingKind.AUTOMATIC));
		dictionary.defineError(new DefaultErrorType("MAN", ErrorRecyclingKind.MANUAL));
		dictionary.defineError(new DefaultErrorType("REJ", ErrorRecyclingKind.NOT_RECYCLABLE));

		{
			SubDictionary importDictionary = dictionary.addSubsetDictionary("import");

			importDictionary.defineError(new DefaultErrorType("TER", ErrorRecyclingKind.AUTOMATIC, 60));
		}
		{
			dictionary.addSubsetDictionary("export");
		}

		testCaseList.forEach(testCase -> {
					LOG.info("Testing " + testCase.name);
					IProcessingResult processingResult = myImportAgent.dispatchMessage(testCase, "test");
					Assert.assertTrue(testCase.name + ", got " + processingResult.getState() + ", " + processingResult.getErrorRecyclingKind(), testCase.isTestValid(processingResult));
				}
		);

		{ // test unknown exception
			processErrorList.add(new DefaultProcessError("UNKNOWN", "Unknown error", "Test"));
			TestCase testCase = new TestCase("Unknown error case", processErrorList, IProcessingResult.State.INVALID, ErrorRecyclingKind.MANUAL);
			boolean raised = false;
			LOG.info("Testing " + testCase.name);
			IProcessingResult processingResult = myImportAgent.dispatchMessage(testCase, "test");
			if (processingResult.getException() instanceof UnknownErrorException) {
				raised = true;
			}
			Assert.assertTrue(raised);
		}

		{ // test export
			processErrorList.clear();
			TestCase testCase = new TestCase("Export error case", processErrorList, IProcessingResult.State.VALID, null);
			LOG.info("Testing " + testCase.name);
			IProcessingResult processingResult = myExportAgent.dispatchMessage(testCase, "test");
			Assert.assertTrue(testCase.name + ", got " + processingResult.getState() + ", " + processingResult.getErrorRecyclingKind(), testCase.isTestValid(processingResult));
		}

		{ // test foreign
			myForeignAgent.work(null, "test");
		}

		{ // test bug
			MyObject obj = new MyObject();
			try {
				myBugguedAgent.work(obj, "test");
			} catch (Exception e) {
				LOG.trace("", e);
			}
			Assert.assertTrue(obj.ok);
			Assert.assertTrue(obj.unknownError);
		}
	}

	private static class MyFlux extends AbstractMMFlux {
        private String msg;
        private TestCase testCase;

        MyFlux(String msg) {
            this.msg = msg;
        }
    }

	private static class MyMMAgent extends AbstractMMAgent<MyFlux> {

		private MyMMAgent() {
			super(MyFlux.class);
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}

		@Override
		public void process(MyFlux message) {
			if (!"ACCEPT".equals(message.msg)) {
				addError(message.msg);
			}
		}

		@Override
		public void reject(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertFalse(getMessage().msg.startsWith("ACCEPT"));
		}

		@Override
		public void accept(Map<IProcessError, ErrorImpact> errorMap) {
			Assert.assertTrue(getMessage().msg.startsWith("ACCEPT"));
		}
	}

	private static class MyImportAgent extends DefaultMMAgent<TestCase> {

		private MyImportAgent() {
			super(TestCase.class);
		}

		@Override
		public void process(TestCase testCase) {
			List<IProcessError> processErrorList = testCase.getErrors();

			processErrorList.forEach(processError -> {
				DefaultProcessError error = (DefaultProcessError) addError(processError.getErrorCode());
				error.setAttribute("");
				error.setValue("");
			});
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}

		@Override
		public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
			try {
				return rootDictionary.getSubsetDictionary("import");
			} catch (UnknownDictionaryException e) {
				return rootDictionary;
			}
		}
	}

	private static class MyExportAgent extends DefaultMMAgent<TestCase> {

		private MyExportAgent() {
			super(TestCase.class);
		}

		@Override
		public void process(TestCase testCase) {
			List<IProcessError> processErrorList = testCase.getErrors();

			processErrorList.forEach(processError -> {
				DefaultProcessError error = (DefaultProcessError) addError(processError.getErrorCode());
				error.setAttribute("");
				error.setValue("");
			});
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.OUT;
		}

		@Override
		public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
			try {
				return rootDictionary.getSubsetDictionary("export");
			} catch (UnknownDictionaryException e) {
				return rootDictionary;
			}
		}
	}

	private static class MyForeignAgent extends DefaultMMAgent<MyFlux> {

		private MyForeignAgent() {
			super(MyFlux.class);
		}

		@Override
		public void process(MyFlux messageObject) {
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}

        @Override
        public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
            Assertions.assertThat(newMessageStatus).isEqualTo(MessageStatus.REJECTED);
        }

        @Override
        public void reject(Map<IProcessError, ErrorImpact> errorMap) {
            super.reject(errorMap);

            Assertions.assertThat(errorMap).hasSize(1);
            Map.Entry<IProcessError, ErrorImpact> next = errorMap.entrySet().iterator().next();
            Assertions.assertThat(next.getKey().getErrorCode()).isEqualTo("UNKNOWN_ERROR");
            Assertions.assertThat(next.getValue().getRecyclingKind()).isEqualTo(ErrorRecyclingKind.NOT_RECYCLABLE);
        }
    }

	private static class MyBugguedAgent extends DefaultMMAgent<MyObject> {

		private MyObject obj;

		private MyBugguedAgent() {
			super(MyObject.class);
		}

		@Override
		public void process(MyObject messageObject) {
			this.obj = messageObject;
			throw new RuntimeException("error");
		}

		@Override
		public void reject(Map<IProcessError, ErrorImpact> errorMap) {
			obj.ok = true;
			errorMap.keySet().forEach(processError -> {
				if ("UNKNOWN_ERROR".equals(processError.getErrorCode())) {
					obj.unknownError = true;
				}
			});
		}

		@Override
		public MessageWay getMessageWay() {
			return MessageWay.IN;
		}
	}

	private class MyObject extends AbstractMMFlux {

		boolean ok;

		boolean unknownError;

	}

	private class TestCase extends AbstractMMFlux {

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
