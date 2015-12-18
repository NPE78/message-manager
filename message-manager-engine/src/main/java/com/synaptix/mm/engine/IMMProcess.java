package com.synaptix.mm.engine;

import java.util.List;
import java.util.Map;

import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.MessageStatus;

/**
 * Created by NicolasP on 30/10/2015.
 */
public interface IMMProcess {

	/**
	 * Core method of an agent, this method processes the message object.
	 * It can raise errors during the process. Those errors will be matched to the dictionary to adapt the behavior of the process accordingly
	 *
	 * @param messageObject The message object received from an Handle
	 */
	void process(Object messageObject);

	/**
	 * The process is in an invalid state, it has been rejected
	 */
	void reject(Map<IProcessError, IErrorType> errorMap);

	/**
	 * The process is in a valid state, it has been accepted.
	 */
	void accept(Map<IProcessError, IErrorType> errorMap);

	/**
	 * Called to notify the implementing class that the message status has to be changed.
	 * If needed, use this to update the message in the database or elsewhere.
	 */
	void notifyMessageStatus(MessageStatus newMessageStatus);

	/**
	 * Get the list of errors raised
	 */
	List<IProcessError> getProcessErrorList();


	/**
	 * The message type name configured for this agent
	 */
	String getMessageTypeName();

	/**
	 * Get the dictionary used to match the errors to their recycling kind
	 */
	default SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
		return rootDictionary;
	}
}
