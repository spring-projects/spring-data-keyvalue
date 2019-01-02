/*
 * Copyright 2014-2019 the original author or authors.
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

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.lang.Nullable;

/**
 * {@link KeyValueAdapter} unifies access and shields the underlying key/value specific implementation.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public interface KeyValueAdapter extends DisposableBean {

	/**
	 * Add object with given id to keyspace.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return the item previously associated with the id.
	 */
	Object put(Object id, Object item, String keyspace);

	/**
	 * Check if a object with given id exists in keyspace.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return true if item of type with id exists.
	 */
	boolean contains(Object id, String keyspace);

	/**
	 * Get the object with given id from keyspace.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return {@literal null} in case no matching item exists.
	 */
	@Nullable
	Object get(Object id, String keyspace);

	/**
	 * Get the object with given id from keyspace.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return {@literal null} in case no matching item exists.
	 * @since 1.1
	 */
	@Nullable
	<T> T get(Object id, String keyspace, Class<T> type);

	/**
	 * Delete and return the object with given type and id.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return {@literal null} if object could not be found
	 */
	@Nullable
	Object delete(Object id, String keyspace);

	/**
	 * Delete and return the object with given type and id.
	 *
	 * @param id must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return {@literal null} if object could not be found
	 * @since 1.1
	 */
	@Nullable
	<T> T delete(Object id, String keyspace, Class<T> type);

	/**
	 * Get all elements for given keyspace.
	 *
	 * @param keyspace must not be {@literal null}.
	 * @return empty {@link Collection} if nothing found.
	 */
	Iterable<?> getAllOf(String keyspace);

	/**
	 * Returns a {@link CloseableIterator} that iterates over all entries.
	 *
	 * @param keyspace must not be {@literal null}.
	 * @return
	 */
	CloseableIterator<Map.Entry<Object, Object>> entries(String keyspace);

	/**
	 * Remove all objects of given type.
	 *
	 * @param keyspace must not be {@literal null}.
	 */
	void deleteAllOf(String keyspace);

	/**
	 * Removes all objects.
	 */
	void clear();

	/**
	 * Find all matching objects within {@literal keyspace}.
	 *
	 * @param query must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return empty {@link Collection} if no match found.
	 */
	Iterable<?> find(KeyValueQuery<?> query, String keyspace);

	/**
	 * @param query must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return empty {@link Collection} if no match found.
	 * @since 1.1
	 */
	<T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type);

	/**
	 * Count number of objects within {@literal keyspace}.
	 *
	 * @param keyspace must not be {@literal null}.
	 * @return
	 */
	long count(String keyspace);

	/**
	 * Count all matching objects within {@literal keyspace}.
	 *
	 * @param query must not be {@literal null}.
	 * @param keyspace must not be {@literal null}.
	 * @return
	 */
	long count(KeyValueQuery<?> query, String keyspace);
}
