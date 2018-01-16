package com.talanlabs.mm.shared.model.domain;

import java.io.Serializable;

/**
 * Created by NicolasP on 29/12/2015.
 */
public class ErrorImpact implements Serializable {

	private final ErrorRecyclingKind recyclingKind;

	private final Integer nextRecyclingDuration;

	public ErrorImpact(ErrorRecyclingKind recyclingKind, Integer nextRecyclingDuration) {
		this.recyclingKind = recyclingKind;
		this.nextRecyclingDuration = nextRecyclingDuration;
	}

	public static ErrorImpact of(ErrorRecyclingKind errorRecyclingKind) {
        return new ErrorImpact(errorRecyclingKind, null);
    }

	/**
	 * The recycling kind induced by the error
	 */
	public ErrorRecyclingKind getRecyclingKind() {
		return recyclingKind;
	}

	/**
	 * The error will ask the message to be recycled after this duration (in minutes) if the message has to be recycled automatically
	 */
	public Integer getNextRecyclingDuration() {
		return nextRecyclingDuration;
	}
}
