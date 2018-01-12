package com.talanlabs.mm.engine;

import com.talanlabs.java.lambda.Try;
import com.talanlabs.mm.engine.exception.InvalidDictionaryOperationException;
import com.talanlabs.mm.engine.exception.UnknownDictionaryException;
import com.talanlabs.mm.engine.exception.UnknownErrorException;
import com.talanlabs.mm.engine.model.IProcessingResult;
import com.talanlabs.mm.engine.model.ProcessingResultBuilder;
import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object contains the configuration of each message type, and is linked to a main dictionary.
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * Created by NicolasP on 29/10/2015.
 */
public class SubDictionary {

	private static final Log LOG = LogFactory.getLog(SubDictionary.class);

	/**
	 * list of known errors for current dictionary
	 */
	private final List<IErrorType> errorTypeList;
	final ReadWriteLock lock;
	private final String dictionaryName;
	private final Map<String, SubDictionary> subsetDictionaryMap;
	private final SubDictionary parentDictionary;
	private boolean burnAfterUse;

	/**
	 * Add a sub dictionary using {@link MMDictionary#addSubsetDictionary(String)}
	 */
	SubDictionary(String name, SubDictionary parentDictionary, ReadWriteLock lock) {
		super();

		this.dictionaryName = name;
		this.parentDictionary = parentDictionary;
		this.lock = lock;

		this.errorTypeList = new ArrayList<>();
		this.subsetDictionaryMap = new HashMap<>();
	}

	/**
	 * If set to true, this dictionary will be destroyed after the {@link #getProcessingResult} method has been called
	 */
	public final void setBurnAfterUse(boolean burnAfterUse) {
		this.burnAfterUse = burnAfterUse;
	}

	/**
	 * Returns the name of the dictionary
	 */
	public final String getDictionaryName() {
		return StringUtils.substring(dictionaryName, dictionaryName.lastIndexOf(".") + 1);
	}

	/**
	 * Returns true if the given dictionary exists in the current sub dictionary. For performances issues, doesn't check dots to get subset of a subset
	 */
	public final boolean existsSubsetDictionary(String dictionaryName) {
		return subsetDictionaryMap.keySet().contains(dictionaryName);
	}

	/**
	 * Get a subset dictionary from current. Use dots to get subset of a subset
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
	 * Add a subset dictionary to the current dictionary. The name is unique, a {@link InvalidDictionaryOperationException} is raised.<br>
	 * The dictionary name cannot be blank. If it is, a {@link InvalidDictionaryOperationException} is raised.
	 */
	public final SubDictionary addSubsetDictionary(String dictionaryName) throws InvalidDictionaryOperationException {
		return getOrCreateSubsetDictionary(dictionaryName, true);
	}

	/**
	 * Add a subset dictionary to the current dictionary. If it already exists, it returns the current subset dictionary<br>
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
				newDictionary = new SubDictionary(this.dictionaryName + "." + name, this, lock); //$NON-NLS-1$
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
				newDictionary = new SubDictionary(this.dictionaryName + "." + dictionaryName, this, lock); //$NON-NLS-1$
				subsetDictionaryMap.put(dictionaryName, newDictionary);
			}
			return newDictionary;
		}
	}

	private void validateDictionaryName(String name) throws InvalidDictionaryOperationException {
		if ("MAIN".equals(name)) { //$NON-NLS-1$
			throw new InvalidDictionaryOperationException("MAIN is reserved");
		}
		if (StringUtils.isBlank(name) || name.matches(".*\\s.*")) { //$NON-NLS-1$
			throw new InvalidDictionaryOperationException("Cannot add subset dictionary with blank(s) in name");
		}
	}

	/**
	 * Build a map which represents the errors managed by this dictionary and its children
	 */
	public final Map<String, IErrorType> getErrorMap() {
		try {
			lock.readLock().lock();
			Map<String, IErrorType> errorMap = new HashMap<>();
			errorTypeList.forEach(errorType -> errorMap.put(errorType.getCode(), errorType));
			subsetDictionaryMap.forEach((s, subDictionary) -> subDictionary.getErrorMap().forEach((s1, errorType) -> errorMap.put(s + "." + s1, errorType)));
			return errorMap;
		} finally {
			lock.readLock().unlock();
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
		return parentDictionary.unregister(dictionaryName.replaceAll("^(.+)\\.", "")); //$NON-NLS-1$ //$NON-NLS-2$
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
		try {
			lock.readLock().lock();
			IProcessingResult processingResult;
			if (errorList == null || errorList.isEmpty()) {
				processingResult = ProcessingResultBuilder.accept();
			} else {
				processingResult = buildProcessingResult(errorList);
			}

			if (burnAfterUse) {
				try {
					destroy();
				} catch (InvalidDictionaryOperationException ex) {
					LOG.error("Couldn't destroy after use", ex);
				}
			}
			return processingResult;
		} finally {
			lock.readLock().unlock();
		}
	}

	private IProcessingResult buildProcessingResult(List<IProcessError> errorList) {
		IProcessingResult processingResult;
		Worst worst = new Worst();

		Map<IProcessError, ErrorImpact> errorMap = new HashMap<>();
		Try<List<Pair<IProcessError, ErrorImpact>>> collect = errorList.stream().map(Try.lazyOf(this::add).andThen(trySupplier -> {
			Try<Pair<IProcessError, ErrorImpact>> pairTry = trySupplier.get();
			if (pairTry.isSuccess()) {
				Pair<IProcessError, ErrorImpact> pair = pairTry.asSuccess().getResult();
				errorMap.put(pair.getKey(), pair.getValue());
				updateWorst(worst, pair.getValue());
			} else if (pairTry.isFailure() && pairTry.asFailure().getException() instanceof UnknownErrorException) {
				IProcessError processError = ((UnknownErrorException) pairTry.asFailure().getException()).getProcessError();
				ErrorImpact errorImpact = new ErrorImpact(ErrorRecyclingKind.MANUAL, null, dictionaryName);
				errorMap.put(processError, errorImpact);
				updateWorst(worst, errorImpact);
			}
			return trySupplier;
		})).collect(Try.collect());
		Exception e = null;
		if (collect.isFailure()) {
			e = collect.asFailure().getException();
		}

		processingResult = compileProcessingResult(worst, errorMap, e);
		return processingResult;
	}

	private Pair<IProcessError, ErrorImpact> add(IProcessError processError) throws UnknownErrorException {
		return Pair.of(processError, computeErrorImpact(processError));
	}

	/**
	 * Defines an error by adding or updating its definition of an error in the current dictionary<br>
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
	private ErrorImpact computeErrorImpact(IProcessError processError) throws UnknownErrorException {
		String errorCode = processError.getErrorCode();
		ErrorImpact errorImpact;
		Optional<IErrorType> first = Optional.empty();
		if (errorTypeList != null) {
			first = errorTypeList.stream().filter(processError::matches).findFirst();
		}
		if (first == null || !first.isPresent()) {
			if (parentDictionary == null) {
				throw new UnknownErrorException(processError, "Error code '" + errorCode + "' not found " + getDictionaryExceptionString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				try {
					return parentDictionary.computeErrorImpact(processError);
				} catch (UnknownErrorException e) {
					throw new UnknownErrorException(processError, "Error code '" + errorCode + "' not found" + getDictionaryExceptionString(), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		IErrorType errorType = first.get();
        errorImpact = new ErrorImpact(errorType.getRecyclingKind(), errorType.getNextRecyclingDuration(), dictionaryName);
        return errorImpact;
	}

	private String getDictionaryExceptionString() {
		return " in dictionary '" + dictionaryName + "'";
	} //$NON-NLS-1$ //$NON-NLS-2$

	private void updateWorst(Worst worst, ErrorImpact errorImpact) {
		worst.errorRecyclingKind = ErrorRecyclingKind.getWorst(errorImpact.getRecyclingKind(), worst.errorRecyclingKind);
		worst.delay = Math.max(errorImpact.getNextRecyclingDuration() != null ? errorImpact.getNextRecyclingDuration() : 0, worst.delay != null ? worst.delay : 0);
	}

	private IProcessingResult compileProcessingResult(Worst worst, Map<IProcessError, ErrorImpact> errorMap, Exception e) {
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
