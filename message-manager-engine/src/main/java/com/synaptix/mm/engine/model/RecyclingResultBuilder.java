package com.synaptix.mm.engine.model;

import java.util.Date;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public final class RecyclingResultBuilder {

	private RecyclingResultBuilder() {
	}

	private IProcessingResult acceptResult() {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.VALID);
		return recyclingResult;
	}

	public static IProcessingResult accept() {
		return new RecyclingResultBuilder().acceptResult();
	}

	private IProcessingResult rejectResult(ErrorRecyclingKind errorRecyclingKind, Date nextProcessingDate) {
		ProcessingResult recyclingResult = new ProcessingResult();
		recyclingResult.setState(IProcessingResult.State.INVALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setNextProcessingDate(nextProcessingDate);
		return recyclingResult;
	}

	public static IProcessingResult reject(ErrorRecyclingKind errorRecyclingKind, Date nextProcessingDate) {
		return new RecyclingResultBuilder().rejectResult(errorRecyclingKind, nextProcessingDate);
	}
}
