package com.talanlabs.mm.engine;

import com.talanlabs.mm.shared.model.IErrorType;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A dictionary is an object which links an error to a way of dealing with it<br>
 * This dictionary is special, it cannot be destroyed.<br>
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * @see #defineError(IErrorType)
 * @see IErrorType
 */
public final class MMDictionary extends SubDictionary {

	/**
	 * The main dictionary is unique
	 */
	public MMDictionary() {
		super("MAIN", null, new ReentrantReadWriteLock()); //$NON-NLS-1$
	}

	/**
	 * Reload the main dictionary in a thread safe method.
	 */
	public void reload(DictionaryBuilder builder) {
		try {
			lock.writeLock().lock();
			clear();
			builder.build();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * This is the callback used by the {@link #reload} method when reloading the dictionary
	 */
	public interface DictionaryBuilder {

		void build();

	}
}
