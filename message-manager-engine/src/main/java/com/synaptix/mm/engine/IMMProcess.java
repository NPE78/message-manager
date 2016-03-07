package com.synaptix.mm.engine;

import java.util.List;
import java.util.Map;

import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.MessageStatus;
import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 30/10/2015.
 */
public interface IMMProcess {

	/**
	 * The process is being initialized
	 */
	default void start() {
	}

	/**
	 * Core method of an agent, this method processes the message object.
	 * It can raise errors during the process. Those errors will be matched to the dictionary to adapt the behavior of the process accordingly
	 *
	 * @param messageObject The message object received from an Handle
	 */
	void process(Object messageObject);

	/**
	 * Close the process finished
	 * @param accepted indicates if the process is being accepted or rejected
	 */
	default void close(boolean accepted) {
	}

	/**
	 * The process is in an invalid state, it has been rejected
	 */
	void reject(Map<IProcessError, ErrorImpact> errorMap);

	/**
	 * The process is in a valid state, it has been accepted.
	 */
	void accept(Map<IProcessError, ErrorImpact> errorMap);

	/**
	 * Called to notify the implementing class that the message status has to be changed.
	 * If needed, use this to update the message in the database or elsewhere.
	 */
	void notifyMessageStatus(MessageStatus newMessageStatus);

	/**
	 * Get the list of errors raised. Used and enriched by the mm engine, see {@link IProcessingResult#getErrorMap()}.
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

	/**
	 * The way of the message: IN or OUT
	 */
	MessageWay getMessageWay();

}
