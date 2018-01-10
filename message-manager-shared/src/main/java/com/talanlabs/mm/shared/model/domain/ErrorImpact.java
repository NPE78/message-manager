package com.talanlabs.mm.shared.model.domain;

/**
 * Created by NicolasP on 29/12/2015.
 */
public class ErrorImpact {

	private final ErrorRecyclingKind recyclingKind;

	private final Integer nextRecyclingDuration;

	private final String dictionaryName;

	public ErrorImpact(ErrorRecyclingKind recyclingKind, Integer nextRecyclingDuration, String dictionaryName) {
		this.recyclingKind = recyclingKind;
		this.nextRecyclingDuration = nextRecyclingDuration;
		this.dictionaryName = dictionaryName;
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

	/**
	 * The dictionary used to compute the impact of this error
	 */
	public String getDictionaryName() {
		return dictionaryName;
	}
}
