package com.synaptix.mm.engine;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synaptix.mm.engine.exception.DictionaryAlreadyDefinedException;
import com.synaptix.mm.engine.exception.UnknownDictionaryException;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.exception.UnknownMessageTypeException;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.engine.model.ProcessingResultBuilder;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * This object contains the configuration of each message type, and is linked to a main dictionary.
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public class SubDictionary {

	/**
	 * Key is the name of the message type, Value is the list of errors known for the message type
	 */
	protected final Map<String, List<IErrorType>> errorTypeMap;

	private final String dictionaryName;

	private final Map<String, SubDictionary> subsetDictionaryMap;

	private final SubDictionary parentDictionary;

	/**
	 * Add a sub dictionary using {@link MMDictionary#addSubsetDictionary(String)}
	 */
	SubDictionary(String name, SubDictionary parentDictionary) {
		super();

		this.dictionaryName = name;
		this.parentDictionary = parentDictionary;

		this.errorTypeMap = new HashMap<>();
		this.subsetDictionaryMap = new HashMap<>();
	}

	/**
	 * Get a subset dictionary from current. Use dots to get subset of a subset
	 */
	public final SubDictionary getSubsetDictionary(String dictionaryName) {
		String[] keys = dictionaryName.split("\\.");
		SubDictionary dictionary = this;
		for (String key : keys) {
			dictionary = dictionary.subsetDictionaryMap.get(key);
			if (dictionary == null) {
				throw new UnknownDictionaryException("'" + key + "' in dictionary '" + dictionaryName + "'");
			}
		}
		return dictionary;
	}

	/**
	 * Add a subset dictionary to the current dictionary. The name is unique, a {@link DictionaryAlreadyDefinedException} is raised
	 */
	public final SubDictionary addSubsetDictionary(String dictionaryName) {
		if (subsetDictionaryMap.containsKey(dictionaryName)) {
			throw new DictionaryAlreadyDefinedException(dictionaryName);
		}
		SubDictionary newDictionary = new SubDictionary(this.dictionaryName + "." + dictionaryName, this);
		subsetDictionaryMap.put(dictionaryName, newDictionary);
		return newDictionary;
	}

	/**
	 * Get the processing result given a message type and a list of errors raised during the process.
	 * It uses the dictionnary to determine whether the process is valid or invalid, computes the recycling kind according to the configuration and if needed a next processing date
	 * If an error is unknown for the message type in the current dictionary or in a parent one, an {@link UnknownErrorException} is raised
	 */
	public final IProcessingResult getProcessingResult(String messageTypeName, List<IProcessError> errorList) {
		checkMessageTypeExistence(messageTypeName);

		if (errorList == null || errorList.isEmpty()) {
			return ProcessingResultBuilder.accept();
		}

		Worst worst = new Worst();

		errorList.forEach(s -> updateWorst(worst, getErrorType(messageTypeName, s)));

		return buildProcessingResult(worst);
	}

	/**
	 * Changes the definition of an error or adds a definition of an error in the current dictionary, for given message type
	 */
	public final void fixError(String messageTypeName, IErrorType errorType) {
		checkMessageTypeExistence(messageTypeName);

		List<IErrorType> errorTypeList = errorTypeMap.get(messageTypeName);
		if (errorTypeList == null) {
			errorTypeList = new ArrayList<>();
			errorTypeMap.put(messageTypeName, errorTypeList);
		}
		Iterator<IErrorType> ite = errorTypeList.iterator();
		while (ite.hasNext()) {
			IErrorType e = ite.next();
			if (e.getCode().equals(errorType.getCode())) {
				ite.remove();
			}
		}
		errorTypeList.add(errorType);
	}

	private void checkMessageTypeExistence(String messageTypeName) {
		SubDictionary parent = this;
		while (parent.parentDictionary != null) {
			parent = parent.parentDictionary;
		}
		if (!parent.errorTypeMap.containsKey(messageTypeName)) {
			throw new UnknownMessageTypeException("The message type '" + messageTypeName + "' does not exist!");
		}
	}

	/**
	 * Find the error type in the current dictionary or in a parent. If not found at all, an {@link UnknownErrorException} is raised
	 */
	private IErrorType getErrorType(String messageTypeName, IProcessError s) {
		List<IErrorType> errorTypeList = errorTypeMap.get(messageTypeName);

		Optional<IErrorType> first = null;
		if (errorTypeList != null) {
			first = errorTypeList.stream().filter(errorType -> errorType.getCode().equals(s.getErrorCode())).findFirst();
		}
		if (first == null || !first.isPresent()) {
			if (parentDictionary == null) {
				throw new UnknownErrorException("Error code '" + s.getErrorCode() + "' not found for message type '" + messageTypeName + "' in dictionary '" + dictionaryName + "'");
			} else {
				try {
					return parentDictionary.getErrorType(messageTypeName, s);
				} catch (UnknownErrorException e) {
					throw new UnknownErrorException("Error code '" + s.getErrorCode() + "' not found for message type '" + messageTypeName + "' in dictionary '" + dictionaryName + "'", e);
				}
			}
		}
		return first.get();
	}

	private void updateWorst(Worst worst, IErrorType errorType) {
		worst.errorRecyclingKind = ErrorRecyclingKind.getWorst(errorType.getRecyclingKind(), worst.errorRecyclingKind);
		worst.delay = Math.max(errorType.getNextRecyclingDuration() != null ? errorType.getNextRecyclingDuration() : 0, worst.delay != null ? worst.delay : 0);
	}

	private IProcessingResult buildProcessingResult(Worst worst) {
		switch (worst.errorRecyclingKind) {
			case AUTOMATIC:
				Instant nextProcessingDate = Instant.now();
				nextProcessingDate.plus(worst.delay, ChronoUnit.MINUTES);
				return ProcessingResultBuilder.rejectAutomatically(nextProcessingDate);
			case MANUAL:
				return ProcessingResultBuilder.rejectManually();
			case NOT_RECYCLABLE:
				return ProcessingResultBuilder.rejectDefinitely();
			default:
				// case of the WARNING enum value
				return ProcessingResultBuilder.acceptWithWarning();
		}
	}

	private class Worst {

		ErrorRecyclingKind errorRecyclingKind;

		Integer delay;

	}
}
