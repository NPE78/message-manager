package com.synaptix.mm.server.unit;

import java.util.List;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.synaptix.component.factory.ComponentFactory;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.factory.IProcessErrorFactory;
import com.synaptix.mm.engine.implem.DefaultProcessErrorFactory;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.server.AbstractMMAgent;
import com.synaptix.mm.server.guice.MMServerModule;
import com.synaptix.mm.server.model.IProcessContext;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

import junit.framework.Assert;

/**
 * Created by NicolasP on 28/10/2015.
 */
public class MMAgentTest {

	@Test
	public void testMMAgent() throws Exception {
		Injector injector = Guice.createInjector(Modules.combine(new MMServerModule("trmt"), new MyModule()));

		List<IErrorType> errorTypeList = injector.getInstance(MMDictionary.class).addMessageType(new DefaultMessageType("TEST"));
		errorTypeList.add(new DefaultErrorType("ACCEPT_WARN", ErrorRecyclingKind.WARNING));
		errorTypeList.add(new DefaultErrorType("REJECT_AUTO", ErrorRecyclingKind.AUTOMATIC));
		errorTypeList.add(new DefaultErrorType("REJECT_MAN", ErrorRecyclingKind.MANUAL));
		errorTypeList.add(new DefaultErrorType("REJECT_DEF", ErrorRecyclingKind.NOT_RECYCLABLE));

		{
			IProcessingResult processingResult = injector.getInstance(MyMMAgent.class).doWork("ACCEPT");
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertNull(processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = injector.getInstance(MyMMAgent.class).doWork("ACCEPT_WARN");
			Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.WARNING, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = injector.getInstance(MyMMAgent.class).doWork("REJECT_AUTO");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, processingResult.getErrorRecyclingKind());
			Assert.assertNotNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = injector.getInstance(MyMMAgent.class).doWork("REJECT_MAN");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
		{
			IProcessingResult processingResult = injector.getInstance(MyMMAgent.class).doWork("REJECT_DEF");
			Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
			Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
			Assert.assertNull(processingResult.getNextProcessingDate());
		}
	}

	private interface MyProcessContext extends IProcessContext {

		String getMsg();

		void setMsg(String msg);

	}

	private static class MyMMAgent extends AbstractMMAgent<MyProcessContext> {

		private MyMMAgent() {
			super("TEST");
		}

		@Override
		public void notifyMessageStatus(MessageStatus newMessageStatus) {
		}

		@Override
		protected boolean isConcerned(Object messageObject) {
			return messageObject instanceof String;
		}

		@Override
		protected MyProcessContext buildProcessContext(Object messageObject) {
			return ComponentFactory.getInstance().createInstance(MyProcessContext.class);
		}

		@Override
		public void process(Object messageObject) {
			String msg = (String) messageObject;
			getProcessContext().setMsg(msg);
			if (!"ACCEPT".equals(msg)) {
				addError(msg, null, null);
			}
		}

		@Override
		public void reject() {
			Assert.assertFalse(getProcessContext().getMsg().startsWith("ACCEPT"));
		}

		@Override
		public void accept() {
			Assert.assertTrue(getProcessContext().getMsg().startsWith("ACCEPT"));
		}
	}

	private static class MyModule extends AbstractSynaptixIntegratorServletModule {

		@Override
		protected void configure() {
			bindAgent(MyMMAgent.class, 5, 10);

			bind(IProcessErrorFactory.class).to(DefaultProcessErrorFactory.class).in(Singleton.class);
		}
	}
}
