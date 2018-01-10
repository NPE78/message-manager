package com.talanlabs.mm.shared.model;

import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Any error type must implement this interface.<br>
 * With the message-manager-engine module, it is added to a dictionary using SubDictionary#defineError
 */
public interface IErrorType {

	/**
	 * The code of the error (unique for a message type)
	 */
	String getCode();

	/**
	 * The recycling kind induced by the error
	 */
	ErrorRecyclingKind getRecyclingKind();

	/**
	 * The message will be recycled after this duration (in minutes), only if the message has to be recycled automatically
	 */
	Integer getNextRecyclingDuration();

}
