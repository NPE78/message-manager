package com.synaptix.mm.engine.unit;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.synaptix.mm.engine.IMMProcess;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.MMEngine;
import com.synaptix.mm.engine.model.DefaultErrorType;
import com.synaptix.mm.engine.model.DefaultMessageType;
import com.synaptix.mm.engine.model.DefaultProcessError;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;
import com.synaptix.mm.shared.model.domain.MessageStatus;

import junit.framework.Assert;

/**
 * Created by NicolasP on 30/10/2015.
 */
public class MMEngineTest {

	@Test
	public void testStart() throws Exception {
		MMEngine engine = new MMEngine();
		MMDictionary dictionary = new MMDictionary();
		engine.setDictionary(dictionary);

		List<IErrorType> errorTypeList = dictionary.addMessageType(new DefaultMessageType("test"));
		errorTypeList.add(new DefaultErrorType("errorCode", ErrorRecyclingKind.AUTOMATIC));

		engine.start(null, new MyProcess());
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
			DefaultProcessError error = new DefaultProcessError("errorCode", "", "");
			return Arrays.asList(error);
		}

		@Override
		public String getMessageTypeName() {
			return "test";
		}
	}
}
