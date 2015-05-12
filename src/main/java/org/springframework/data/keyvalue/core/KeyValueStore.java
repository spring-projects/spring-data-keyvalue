/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.keyvalue.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * A key-value store that supports put, get, delete, and queries.
 * 
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @param <K>
 * @param <V>
 */
public interface KeyValueStore<K extends Serializable, V> {

	/**
	 * Add object with given key.
	 * 
	 * @param key must not be {@literal null}.
	 * @return the item previously associated with the key.
	 */
	V put(K key, V item);

	/**
	 * Check if given key is present in KeyValue store.
	 * 
	 * @param key must not be {@literal null}.
	 * @return
	 */
	boolean contains(K key);

	/**
	 * Get the Object associated with the given key.
	 * 
	 * @param key must not be {@literal null}.
	 * @return {@literal null} if key not available.
	 */
	V get(K key);

	/**
	 * Get all Objects associated with the given keys.
	 * 
	 * @param keys must not be {@literal null}.
	 * @return an empty {@link Iterable} if no matching key found.
	 */
	Iterable<V> getAll(Collection<K> keys);

	/**
	 * Delete and return the element associated with the given key.
	 * 
	 * @param key must not be {@literal null}.
	 * @return
	 */
	V remove(K key);

	/**
	 * Delete and return all elements associated with the given keys.
	 * 
	 * @param keys must not be {@literal null}.
	 * @return
	 */
	Iterable<V> removeAll(Collection<K> keys);

	/**
	 * Get the number of total elements contained.
	 * 
	 * @return
	 */
	long size();

	/**
	 * Removes all elements. The {@link KeyValueStore} will be empty after this call returns.
	 */
	void clear();

}
