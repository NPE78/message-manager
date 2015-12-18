package com.synaptix.mm.engine.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * This implementation provides static methods to build a processing result, defined by its state, recycling kind and next processing date
 * Created by NicolasP on 22/10/2015.
 */
public final class ProcessingResultBuilder {

	private Map<IProcessError, IErrorType> errorMap;

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
	public static IProcessingResult acceptWithWarning(Map<IProcessError, IErrorType> errorMap) {
		return new ProcessingResultBuilder().errors(errorMap).acceptResult(ErrorRecyclingKind.WARNING);
	}

	/**
	 * Mark the processing result as being rejected with a manual recycling kind
	 */
	public static IProcessingResult rejectManually(Map<IProcessError, IErrorType> errorMap) {
		return new ProcessingResultBuilder().errors(errorMap).rejectResult(ErrorRecyclingKind.MANUAL, null);
	}

	/**
	 * Mark the processing result as being rejected with an automatic recycling kind
	 */
	public static IProcessingResult rejectAutomatically(Instant nextProcessingDate, Map<IProcessError, IErrorType> errorMap) {
		return new ProcessingResultBuilder().errors(errorMap).rejectResult(ErrorRecyclingKind.AUTOMATIC, nextProcessingDate);
	}

	/**
	 * Mark the processing result as being rejected definitely
	 */
	public static IProcessingResult rejectDefinitely(Map<IProcessError, IErrorType> errorMap) {
		return new ProcessingResultBuilder().errors(errorMap).rejectResult(ErrorRecyclingKind.NOT_RECYCLABLE, null);
	}

	/**
	 * Add a map of errors associated to their type
	 */
	private ProcessingResultBuilder errors(Map<IProcessError, IErrorType> errorMap) {
		this.errorMap = errorMap;
		return this;
	}

	private IProcessingResult acceptResult(ErrorRecyclingKind errorRecyclingKind) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.VALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setErrorMap(errorMap);
		return recyclingResult;
	}

	private IProcessingResult rejectResult(ErrorRecyclingKind errorRecyclingKind, Instant nextProcessingDate) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.INVALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setNextProcessingDate(nextProcessingDate);
		recyclingResult.setErrorMap(errorMap);
		return recyclingResult;
	}
}
