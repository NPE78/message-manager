package com.synaptix.mm.engine.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public final class ProcessingResultBuilder {

	private ProcessingResultBuilder() {
	}

	private IProcessingResult acceptResult() {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.VALID);
		return recyclingResult;
	}

	/**
	 * Mark the processing result as being accepted
	 */
	public static IProcessingResult accept() {
		return new ProcessingResultBuilder().acceptResult();
	}

	private IProcessingResult rejectResult(ErrorRecyclingKind errorRecyclingKind, Instant nextProcessingDate) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.INVALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setNextProcessingDate(nextProcessingDate);
		return recyclingResult;
	}

	/**
	 * Mark the processing result as being rejected with given error recycling kind and if "AUTOMATIC", given next processing date
	 */
	public static IProcessingResult reject(ErrorRecyclingKind errorRecyclingKind, Instant nextProcessingDate) {
		return new ProcessingResultBuilder().rejectResult(errorRecyclingKind, nextProcessingDate);
	}
}
