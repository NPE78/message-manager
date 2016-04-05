package com.synaptix.mm.engine;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.inject.Inject;

/**
 * This dictionary is special, it cannot be destroyed.
 * Subsets dictionaries can be added by using {@link #addSubsetDictionary}
 * Created by NicolasP on 22/10/2015.
 */
public final class MMDictionary extends SubDictionary {

	/**
	 * Use Guice to create this class. The main dictionary is unique
	 */
	@Inject
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
