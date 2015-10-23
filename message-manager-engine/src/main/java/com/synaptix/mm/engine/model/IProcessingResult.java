package com.synaptix.mm.engine.model;

import java.util.Date;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Interface which represents the result of a process (integration or export).
 * Use ProcessingResultBuilder to build
 *
 * Created by NicolasP on 22/10/2015.
 */
public interface IProcessingResult {

	public enum State {

		/**
		 * The message is valid
		 */
		VALID,
		/**
		 * The message is invalid (a recycling kind should be provided)
		 */
		INVALID;

	}

	public State getState();

	public ErrorRecyclingKind getErrorRecyclingKind();

	public Date getNextProcessingDate();

}
