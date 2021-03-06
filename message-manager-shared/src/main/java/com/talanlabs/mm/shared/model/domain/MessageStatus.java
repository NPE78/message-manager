package com.talanlabs.mm.shared.model.domain;

/**
 * Created by NicolasP on 22/10/2015.
 */
public enum MessageStatus {

	/**
	 * The message has to be processed, ie integrated or sent (either it's its first time, either it's asked from an user who asked the message to be recycled).
	 */
	TO_BE_PROCESSED(0),
	/**
	 * The message is currently in progress.
	 * Transient status
	 */
	IN_PROGRESS(1),
	/**
	 * The message can be recycled manually
	 */
	TO_RECYCLE_MANUALLY(3),
	/**
	 * The message will be recycled automatically according to the next processing date
	 */
	TO_RECYCLE_AUTOMATICALLY(3),
	/**
	 * The message has successfully been processed (sent or integrated)
	 */
	PROCESSED(2),
	/**
	 * The message has definitely been rejected
	 */
	REJECTED(4),
	/**
	 * The message has been cancelled
	 */
	CANCELLED(4);

	private final int step;

	MessageStatus(int step) {
		this.step = step;
	}

	public int getStep() {
		return step;
	}
}
