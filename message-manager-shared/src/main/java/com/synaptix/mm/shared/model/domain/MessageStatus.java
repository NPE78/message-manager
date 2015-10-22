package com.synaptix.mm.shared.model.domain;

/**
 * Created by NicolasP on 22/10/2015.
 */
public enum MessageStatus {

	/**
	 * The message has to be integrated (either it's its first time, either it's asked from an user who asked the message to be recycled).
	 */
	TO_BE_INTEGRATED,
	/**
	 * The message has to be sent (either it's its first time, either it's asked from an user who asked the message to be recycled)
	 */
	TO_SEND,
	/**
	 * The message is currently in progress.
	 * Transient status
	 */
	IN_PROGRESS,
	/**
	 * The message can be recycled manually
	 */
	TO_RECYCLE_MANUALLY,
	/**
	 * The message will be recycled automatically according to the next processing date
	 */
	TO_RECYCLE_AUTOMATICALLY,
	/**
	 * The message has successfully been integrated
	 */
	INTEGRATED,
	/**
	 * The message has successfully been sent
	 */
	SENT,
	/**
	 * The message has definitely been rejected
	 */
	REJECTED;

}
