package com.synaptix.mm.shared.model.domain;

/**
 * Created by NicolasP on 22/10/2015.
 */
public enum ErrorRecyclingKind {

	/**
	 * The error doesn't prevent the message to be integrated
	 */
	WARNING(0),
	/**
	 * The error will require an automatic recycling, if no error (manual or not recyclable) has been raised
	 */
	AUTOMATIC(1),
	/**
	* The error will require a manual recycling, if no error (not recyclable) has been raised
	*/
	MANUAL(2),
	/**
	 * The error will definitely reject the message
	 */
	NOT_RECYCLABLE(3);

	private final int criticity;

	private ErrorRecyclingKind(int criticity) {
		this.criticity = criticity;
	}

	public int getCriticity() {
		return criticity;
	}

	public static ErrorRecyclingKind getWorst(ErrorRecyclingKind e1, ErrorRecyclingKind e2) {
		if (e1 == null) {
			return e2;
		}
		if (e2 == null) {
			return e1;
		}
		if (e2.criticity > e1.criticity) {
			return e2;
		}
		return e1;
	}
}
