/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.Optional;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.mapping.context.MappingContext;

/**
 * Interface that specifies a basic set of key/value operations. Implemented by {@link KeyValueTemplate}.
 * 
 * @author Christoph Strobl
 */
public interface KeyValueOperations extends DisposableBean {

	/**
	 * Add given object. Object needs to have id property to which a generated value will be assigned.
	 * 
	 * @param objectToInsert
	 * @return
	 */
	<T> T insert(T objectToInsert);

	/**
	 * Add object with given id.
	 * 
	 * @param id must not be {@literal null}.
	 * @param objectToInsert must not be {@literal null}.
	 */
	void insert(Object id, Object objectToInsert);

	/**
	 * Get all elements of given type. Respects {@link KeySpace} if present and therefore returns all elements that can be
	 * assigned to requested type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return empty iterable if no elements found.
	 */
	<T> Iterable<T> findAll(Class<T> type);

	/**
	 * Get all elements ordered by sort. Respects {@link KeySpace} if present and therefore returns all elements that can
	 * be assigned to requested type.
	 * 
	 * @param sort must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return
	 */
	<T> Iterable<T> findAll(Sort sort, Class<T> type);

	/**
	 * Get element of given type with given id. Respects {@link KeySpace} if present and therefore returns all elements
	 * that can be assigned to requested type.
	 * 
	 * @param id must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return null if not found.
	 */
	<T> Optional<T> findById(Object id, Class<T> type);

	/**
	 * Execute operation against underlying store.
	 * 
	 * @param action must not be {@literal null}.
	 * @return
	 */
	<T> T execute(KeyValueCallback<T> action);

	/**
	 * Get all elements matching the given query. <br />
	 * Respects {@link KeySpace} if present and therefore returns all elements that can be assigned to requested type..
	 * 
	 * @param query must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return empty iterable if no match found.
	 */
	<T> Iterable<T> find(KeyValueQuery<?> query, Class<T> type);

	/**
	 * Get all elements in given range. Respects {@link KeySpace} if present and therefore returns all elements that can
	 * be assigned to requested type.
	 * 
	 * @param offset
	 * @param rows
	 * @param type must not be {@literal null}.
	 * @return
	 */
	<T> Iterable<T> findInRange(long offset, int rows, Class<T> type);

	/**
	 * Get all elements in given range ordered by sort. Respects {@link KeySpace} if present and therefore returns all
	 * elements that can be assigned to requested type.
	 * 
	 * @param offset
	 * @param rows
	 * @param sort
	 * @param type
	 * @return
	 */
	<T> Iterable<T> findInRange(long offset, int rows, Sort sort, Class<T> type);

	/**
	 * @param objectToUpdate must not be {@literal null}.
	 */
	void update(Object objectToUpdate);

	/**
	 * @param id must not be {@literal null}.
	 * @param objectToUpdate must not be {@literal null}.
	 */
	void update(Object id, Object objectToUpdate);

	/**
	 * Remove all elements of type. Respects {@link KeySpace} if present and therefore removes all elements that can be
	 * assigned to requested type.
	 * 
	 * @param type must not be {@literal null}.
	 */
	void delete(Class<?> type);

	/**
	 * @param objectToDelete must not be {@literal null}.
	 * @return
	 */
	<T> T delete(T objectToDelete);

	/**
	 * Delete item of type with given id.
	 * 
	 * @param id must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return the deleted item or {@literal null} if no match found.
	 */
	<T> T delete(Object id, Class<T> type);

	/**
	 * Total number of elements with given type available. Respects {@link KeySpace} if present and therefore counts all
	 * elements that can be assigned to requested type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	long count(Class<?> type);

	/**
	 * Total number of elements matching given query. Respects {@link KeySpace} if present and therefore counts all
	 * elements that can be assigned to requested type.
	 * 
	 * @param query
	 * @param type
	 * @return
	 */
	long count(KeyValueQuery<?> query, Class<?> type);

	/**
	 * @return mapping context in use.
	 */
	MappingContext<?, ?> getMappingContext();
}
