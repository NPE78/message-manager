package com.synaptix.mm.engine.model;

import java.time.Instant;
import java.util.Map;

import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Interface which represents the result of a process (integration or export), defined by its state, recycling kind and next processing date.
 * Use {@link ProcessingResultBuilder} to build
 * <p>
 * Created by NicolasP on 22/10/2015.
 */
public interface IProcessingResult {

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

	/**
	 * A map of errors returned by the process associated to their type
	 */
	Map<IProcessError, ErrorImpact> getErrorMap();

	/**
	 * If the process had an exception when building this result, it can be used with this method
	 */
	Exception getException();

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

}
