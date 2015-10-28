package com.synaptix.mm.engine.unit;

import java.time.Instant;

import org.junit.Test;

import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.engine.model.ProcessingResultBuilder;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

import junit.framework.Assert;

/**
 * Created by NicolasP on 28/10/2015.
 */
public class ProcessingResultBuilderTest {

	@Test
	public void testProcessingResultBuilder() throws Exception {
		IProcessingResult processingResult;
		processingResult = ProcessingResultBuilder.accept();
		Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
		Assert.assertNull(processingResult.getNextProcessingDate());

		processingResult = ProcessingResultBuilder.acceptWithWarning();
		Assert.assertEquals(IProcessingResult.State.VALID, processingResult.getState());
		Assert.assertEquals(ErrorRecyclingKind.WARNING, processingResult.getErrorRecyclingKind());
		Assert.assertNull(processingResult.getNextProcessingDate());

		Instant now = Instant.now();
		processingResult = ProcessingResultBuilder.rejectAutomatically(now);
		Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
		Assert.assertEquals(ErrorRecyclingKind.AUTOMATIC, processingResult.getErrorRecyclingKind());
		Assert.assertEquals(now, processingResult.getNextProcessingDate());

		processingResult = ProcessingResultBuilder.rejectManually();
		Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
		Assert.assertEquals(ErrorRecyclingKind.MANUAL, processingResult.getErrorRecyclingKind());
		Assert.assertNull(processingResult.getNextProcessingDate());

		processingResult = ProcessingResultBuilder.rejectDefinitely();
		Assert.assertEquals(IProcessingResult.State.INVALID, processingResult.getState());
		Assert.assertEquals(ErrorRecyclingKind.NOT_RECYCLABLE, processingResult.getErrorRecyclingKind());
		Assert.assertNull(processingResult.getNextProcessingDate());
	}
}
