package com.synaptix.mm.engine.model;

import java.time.Instant;
import java.util.Map;

import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Default implementation of {@link IProcessingResult}.
 * Processing result built by {@link ProcessingResultBuilder}
 *
 * Created by NicolasP on 22/10/2015.
 */
final class ProcessingResult implements IProcessingResult {

	private IProcessingResult.State state;

	private ErrorRecyclingKind errorRecyclingKind;

	private Instant nextProcessingDate;

	private Map<IProcessError, ErrorImpact> errorMap;

	ProcessingResult() {
	}

	@Override
	public IProcessingResult.State getState() {
		return state;
	}

	public void setState(IProcessingResult.State state) {
		this.state = state;
	}

	@Override
	public ErrorRecyclingKind getErrorRecyclingKind() {
		return errorRecyclingKind;
	}

	public void setErrorRecyclingKind(ErrorRecyclingKind errorRecyclingKind) {
		this.errorRecyclingKind = errorRecyclingKind;
	}

	@Override
	public Instant getNextProcessingDate() {
		return nextProcessingDate;
	}

	public void setNextProcessingDate(Instant nextProcessingDate) {
		this.nextProcessingDate = nextProcessingDate;
	}

	@Override
	public Map<IProcessError, ErrorImpact> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<IProcessError, ErrorImpact> errorMap) {
		this.errorMap = errorMap;
	}
}
