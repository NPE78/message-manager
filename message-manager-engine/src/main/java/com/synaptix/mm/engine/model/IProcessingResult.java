package com.synaptix.mm.engine.model;

import java.time.Instant;

import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Interface which represents the result of a process (integration or export), defined by its state, recycling kind and next processing date.
 * Use {@link ProcessingResultBuilder} to build
 *
 * Created by NicolasP on 22/10/2015.
 */
public interface IProcessingResult {

	enum State {

		/**
		 * The message is valid
		 */
		VALID,
		/**
		 * The message is invalid (an error recycling kind should be provided)
		 */
		INVALID

	}

	/**
	 * The state can either be VALID or INVALID. VALID means the message is successful and should be accepted. INVALID means the message should be recycled or rejected definitely
	 */
	State getState();

	/**
	 * If the state is INVALID, the following recycling kind is provided
	 */
	ErrorRecyclingKind getErrorRecyclingKind();

	/**
	 * If the recycling kind is AUTOMATIC, the following next processing date is provided
	 */
	Instant getNextProcessingDate();

}
