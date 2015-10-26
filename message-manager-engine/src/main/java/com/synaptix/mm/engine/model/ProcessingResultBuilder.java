package com.synaptix.mm.engine.model;

import java.time.Instant;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public final class ProcessingResultBuilder {

	private ProcessingResultBuilder() {
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
	public static IProcessingResult acceptWithWarning() {
		return new ProcessingResultBuilder().acceptResult(ErrorRecyclingKind.WARNING);
	}

	/**
	 * Mark the processing result as being rejected with a manual recycling kind
	 */
	public static IProcessingResult rejectManually() {
		return new ProcessingResultBuilder().rejectResult(ErrorRecyclingKind.MANUAL, null);
	}

	/**
	 * Mark the processing result as being rejected with an automatic recycling kind
	 * @param nextProcessingDate
	 */
	public static IProcessingResult rejectAutomatically(Instant nextProcessingDate) {
		return new ProcessingResultBuilder().rejectResult(ErrorRecyclingKind.AUTOMATIC, nextProcessingDate);
	}

	/**
	 * Mark the processing result as being rejected definitely
	 */
	public static IProcessingResult rejectDefinitely() {
		return new ProcessingResultBuilder().rejectResult(ErrorRecyclingKind.NOT_RECYCLABLE, null);
	}

	private IProcessingResult acceptResult(ErrorRecyclingKind errorRecyclingKind) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.VALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		return recyclingResult;
	}

	private IProcessingResult rejectResult(ErrorRecyclingKind errorRecyclingKind, Instant nextProcessingDate) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.INVALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setNextProcessingDate(nextProcessingDate);
		return recyclingResult;
	}
}
