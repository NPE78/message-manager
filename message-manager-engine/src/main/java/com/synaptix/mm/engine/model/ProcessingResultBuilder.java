package com.synaptix.mm.engine.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * This implementation provides static methods to build a processing result, defined by its state, recycling kind and next processing date
 * Created by NicolasP on 22/10/2015.
 */
public final class ProcessingResultBuilder {

	private Map<IProcessError, ErrorImpact> errorMap;

	private Exception exception;

	private ProcessingResultBuilder() {
		this.errorMap = new HashMap<>(0);
	}

	/**
	 * Mark the processing result as being accepted without any error
	 */
	public static IProcessingResult accept() {
		return new ProcessingResultBuilder().acceptResult(null);
	}

	/**
	 * Mark the processing result as being accepted with a warning
	 */
	public static IProcessingResult acceptWithWarning(Map<IProcessError, ErrorImpact> errorMap, Exception exception) {
		return new ProcessingResultBuilder().errors(errorMap).exception(exception).acceptResult(ErrorRecyclingKind.WARNING);
	}

	/**
	 * Mark the processing result as being rejected with a manual recycling kind
	 */
	public static IProcessingResult rejectManually(Map<IProcessError, ErrorImpact> errorMap, Exception exception) {
		return new ProcessingResultBuilder().errors(errorMap).exception(exception).rejectResult(ErrorRecyclingKind.MANUAL, null);
	}

	/**
	 * Mark the processing result as being rejected with an automatic recycling kind
	 */
	public static IProcessingResult rejectAutomatically(Instant nextProcessingDate, Map<IProcessError, ErrorImpact> errorMap, Exception exception) {
		return new ProcessingResultBuilder().errors(errorMap).exception(exception).rejectResult(ErrorRecyclingKind.AUTOMATIC, nextProcessingDate);
	}

	/**
	 * Mark the processing result as being rejected definitely
	 */
	public static IProcessingResult rejectDefinitely(Map<IProcessError, ErrorImpact> errorMap, Exception exception) {
		return new ProcessingResultBuilder().errors(errorMap).exception(exception).rejectResult(ErrorRecyclingKind.NOT_RECYCLABLE, null);
	}

	/**
	 * Defines the map of errors associated to their impact
	 */
	private ProcessingResultBuilder errors(Map<IProcessError, ErrorImpact> errorMap) {
		this.errorMap = errorMap;
		return this;
	}

	/**
	 * Defines the exception, null if none has been raised
	 */
	private ProcessingResultBuilder exception(Exception exception) {
		this.exception = exception;
		return this;
	}

	private IProcessingResult acceptResult(ErrorRecyclingKind errorRecyclingKind) {
		ProcessingResult processingResult = new ProcessingResult();
		processingResult.setState(IProcessingResult.State.VALID);
		processingResult.setErrorRecyclingKind(errorRecyclingKind);
		processingResult.setErrorMap(errorMap);
		processingResult.setException(exception);
		return processingResult;
	}

	private IProcessingResult rejectResult(ErrorRecyclingKind errorRecyclingKind, Instant nextProcessingDate) {
		ProcessingResult processingResult = new ProcessingResult();
		processingResult.setState(IProcessingResult.State.INVALID);
		processingResult.setErrorRecyclingKind(errorRecyclingKind);
		processingResult.setNextProcessingDate(nextProcessingDate);
		processingResult.setErrorMap(errorMap);
		processingResult.setException(exception);
		return processingResult;
	}
}
