package com.synaptix.mm.engine;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.synaptix.java.lambda.Try;
import com.synaptix.mm.engine.exception.InvalidDictionaryOperationException;
import com.synaptix.mm.engine.exception.UnknownDictionaryException;
import com.synaptix.mm.engine.exception.UnknownErrorException;
import com.synaptix.mm.engine.model.IProcessingResult;
import com.synaptix.mm.engine.model.ProcessingResultBuilder;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.shared.model.IProcessError;
import com.synaptix.mm.shared.model.domain.ErrorImpact;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * This object contains the configuration of each message type, and is linked to a main dictionary.
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public class SubDictionary {

	/**
	 * list of known errors for current dictionary
	 */
	protected final List<IErrorType> errorTypeList;

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

		this.errorTypeList = new ArrayList<>();
		this.subsetDictionaryMap = new HashMap<>();
	}

	/**
	 * Get a subset dictionary from current. Use dots to get subset of a subset<br/>
	 * An {@link UnknownDictionaryException} is thrown if the dictionary is unknown
	 */
	public final SubDictionary getSubsetDictionary(String dictionaryName) throws UnknownDictionaryException {
		if (StringUtils.isBlank(dictionaryName)) {
			throw new UnknownDictionaryException("Dictionary name is null or blank");
		}
		String[] keys = dictionaryName.split("\\."); //$NON-NLS-1$
		SubDictionary dictionary = this;
		for (String key : keys) {
			dictionary = dictionary.subsetDictionaryMap.get(key);
			if (dictionary == null) {
				throw new UnknownDictionaryException("'" + key + "'" + getDictionaryExceptionString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return dictionary;
	}

	/**
	 * Add a subset dictionary to the current dictionary. The name is unique, a {@link InvalidDictionaryOperationException} is raised.<br/>
	 * The dictionary name cannot be blank. If it is, a {@link InvalidDictionaryOperationException} is raised.
	 */
	public final SubDictionary addSubsetDictionary(String dictionaryName) throws InvalidDictionaryOperationException {
		return getOrCreateSubsetDictionary(dictionaryName, true);
	}

	/**
	 * Add a subset dictionary to the current dictionary. If it already exists, it returns the current subset dictionary<br/>
	 */
	public final SubDictionary getOrCreateSubsetDictionary(String dictionaryName) throws InvalidDictionaryOperationException {
		return getOrCreateSubsetDictionary(dictionaryName, false);
	}

	/**
	 * checkUnique is true if an exception should be raised if the dictionary already exists
	 */
	private SubDictionary getOrCreateSubsetDictionary(String dictionaryName, boolean checkUnique) throws InvalidDictionaryOperationException {
		validateDictionaryName(dictionaryName);

		int idx = dictionaryName.indexOf("."); //$NON-NLS-1$
		if (idx > -1) {
			String name = dictionaryName.substring(0, idx);

			validateDictionaryName(name);

			SubDictionary newDictionary = subsetDictionaryMap.get(name);
			if (newDictionary == null) {
				newDictionary = new SubDictionary(this.dictionaryName + "." + name, this); //$NON-NLS-1$
				subsetDictionaryMap.put(name, newDictionary);
			}
			return newDictionary.getOrCreateSubsetDictionary(StringUtils.substring(dictionaryName, idx + 1), checkUnique);
		} else {
			SubDictionary newDictionary = subsetDictionaryMap.get(dictionaryName);
			if (newDictionary != null) {
				if (checkUnique) {
					throw new InvalidDictionaryOperationException(dictionaryName + " is already defined");
				}
			} else {
				newDictionary = new SubDictionary(this.dictionaryName + "." + dictionaryName, this); //$NON-NLS-1$
				subsetDictionaryMap.put(dictionaryName, newDictionary);
			}
			return newDictionary;
		}
	}

	private void validateDictionaryName(String name) throws InvalidDictionaryOperationException {
		if ("MAIN".equals(name)) { //$NON-NLS-1$
			throw new InvalidDictionaryOperationException("MAIN is reserved");
		}
		if (StringUtils.isBlank(name)) {
			throw new InvalidDictionaryOperationException("addSubsetDictionary with blank name");
		}
	}

	/**
	 * Destroy a dictionary, so that it cannot be used anymore
	 */
	public final boolean destroy() throws InvalidDictionaryOperationException {
		if (parentDictionary == null) {
			throw new InvalidDictionaryOperationException("Cannot destroy the main dictionary");
		}
		clear();
		return parentDictionary.unregister(dictionaryName.replaceAll("^(.+)\\.", ""));
	}

	private boolean unregister(String dictionaryName) {
		return subsetDictionaryMap.remove(dictionaryName) != null;
	}

	/**
	 * Get the processing result given a message type and a list of errors raised during the process.
	 * It uses the dictionnary to determine whether the process is valid or invalid, computes the recycling kind according to the configuration and if needed a next processing date
	 * If an error is unknown for the message type in the current dictionary or in a parent one, an {@link UnknownErrorException} is raised
	 */
	public final IProcessingResult getProcessingResult(List<IProcessError> errorList) {
		if (errorList == null || errorList.isEmpty()) {
			return ProcessingResultBuilder.accept();
		}

		Worst worst = new Worst();

		Map<IProcessError, ErrorImpact> errorMap = new HashMap<>();
		Try<List<Pair<IProcessError, ErrorImpact>>> collect = errorList.stream().map(Try.lazyOf(this::add).andThen(trySupplier -> {
			Try<Pair<IProcessError, ErrorImpact>> pairTry = trySupplier.get();
			if (pairTry.isSuccess()) {
				Pair<IProcessError, ErrorImpact> pair = pairTry.asSuccess().getResult();
				errorMap.put(pair.getKey(), pair.getValue());
				updateWorst(worst, pair.getValue());
			}
			return trySupplier;
		})).collect(Try.collect());
		Exception e = null;
		if (collect.isFailure()) {
			e = collect.asFailure().getException();
		}
		return buildProcessingResult(worst, errorMap, e);
	}

	private Pair<IProcessError, ErrorImpact> add(IProcessError processError) throws UnknownErrorException {
		return Pair.of(processError, computeErrorImpact(processError.getErrorCode()));
	}

	/**
	 * Defines an error by adding or updating its definition of an error in the current dictionary<br/>
	 * Returns true if the error has been overwritten, false otherwise
	 */
	public final boolean defineError(IErrorType errorType) {
		boolean overwritten = false;
		Iterator<IErrorType> ite = errorTypeList.iterator();
		while (ite.hasNext()) {
			IErrorType e = ite.next();
			if (e.getCode().equals(errorType.getCode())) {
				overwritten = true;
				ite.remove();
			}
		}
		errorTypeList.add(errorType);
		return overwritten;
	}

	/**
	 * Clear the dictionary from all errors and subset dictionaries
	 */
	public void clear() {
		errorTypeList.clear();
		subsetDictionaryMap.clear();
	}

	/**
	 * Find the error type in the current dictionary or in a parent. If not found at all, an {@link UnknownErrorException} is raised
	 */
	private ErrorImpact computeErrorImpact(String errorCode) throws UnknownErrorException {
		ErrorImpact errorImpact = null;
		Optional<IErrorType> first = null;
		if (errorTypeList != null) {
			first = errorTypeList.stream().filter(errorType -> errorType.getCode().equals(errorCode)).findFirst();
		}
		if (first == null || !first.isPresent()) {
			if (parentDictionary == null) {
				throw new UnknownErrorException("Error code '" + errorCode + "' not found " + getDictionaryExceptionString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				try {
					return parentDictionary.computeErrorImpact(errorCode);
				} catch (UnknownErrorException e) {
					throw new UnknownErrorException("Error code '" + errorCode + "' not found" + getDictionaryExceptionString(), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		IErrorType errorType = first.get();
		if (errorType != null) {
			errorImpact = new ErrorImpact(errorType.getRecyclingKind(), errorType.getNextRecyclingDuration(), dictionaryName);
		}
		return errorImpact;
	}

	private String getDictionaryExceptionString() {
		return " in dictionary '" + dictionaryName + "'";
	} //$NON-NLS-1$ //$NON-NLS-2$

	private void updateWorst(Worst worst, ErrorImpact errorImpact) {
		worst.errorRecyclingKind = ErrorRecyclingKind.getWorst(errorImpact.getRecyclingKind(), worst.errorRecyclingKind);
		worst.delay = Math.max(errorImpact.getNextRecyclingDuration() != null ? errorImpact.getNextRecyclingDuration() : 0, worst.delay != null ? worst.delay : 0);
	}

	private IProcessingResult buildProcessingResult(Worst worst, Map<IProcessError, ErrorImpact> errorMap, Exception e) {
		switch (worst.errorRecyclingKind) {
			case AUTOMATIC:
				Instant nextProcessingDate = Instant.now();
				nextProcessingDate.plus(worst.delay, ChronoUnit.MINUTES);
				return ProcessingResultBuilder.rejectAutomatically(nextProcessingDate, errorMap, e);
			case MANUAL:
				return ProcessingResultBuilder.rejectManually(errorMap, e);
			case NOT_RECYCLABLE:
				return ProcessingResultBuilder.rejectDefinitely(errorMap, e);
			default:
				// case of the WARNING enum value
				return ProcessingResultBuilder.acceptWithWarning(errorMap, e);
		}
	}

	private class Worst {

		ErrorRecyclingKind errorRecyclingKind;

		Integer delay;

	}
}
