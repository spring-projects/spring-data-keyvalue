/*
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.keyvalue.core;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.lang.Nullable;

/**
 * Base implementation for accessing and executing {@link KeyValueQuery} against a {@link KeyValueAdapter}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @param <ADAPTER>
 * @param <CRITERIA>
 * @param <SORT>
 */
public abstract class QueryEngine<ADAPTER extends KeyValueAdapter, CRITERIA, SORT> {

	private final Optional<CriteriaAccessor<CRITERIA>> criteriaAccessor;
	private final Optional<SortAccessor<SORT>> sortAccessor;

	private @Nullable ADAPTER adapter;

	public QueryEngine(@Nullable CriteriaAccessor<CRITERIA> criteriaAccessor, @Nullable SortAccessor<SORT> sortAccessor) {

		this.criteriaAccessor = Optional.ofNullable(criteriaAccessor);
		this.sortAccessor = Optional.ofNullable(sortAccessor);
	}

	/**
	 * Extract query attributes and delegate to concrete execution.
	 *
	 * @param query
	 * @param keyspace
	 * @return
	 */
	public Collection<?> execute(KeyValueQuery<?> query, String keyspace) {

		CRITERIA criteria = this.criteriaAccessor.map(it -> it.resolve(query)).orElse(null);
		SORT sort = this.sortAccessor.map(it -> it.resolve(query)).orElse(null);

		return execute(criteria, sort, query.getOffset(), query.getRows(), keyspace);
	}

	/**
	 * Extract query attributes and delegate to concrete execution.
	 *
	 * @param query
	 * @param keyspace
	 * @return
	 */
	public <T> Collection<T> execute(KeyValueQuery<?> query, String keyspace, Class<T> type) {

		CRITERIA criteria = this.criteriaAccessor.map(it -> it.resolve(query)).orElse(null);
		SORT sort = this.sortAccessor.map(it -> it.resolve(query)).orElse(null);

		return execute(criteria, sort, query.getOffset(), query.getRows(), keyspace, type);
	}

	/**
	 * Extract query attributes and delegate to concrete execution.
	 *
	 * @param query
	 * @param keyspace
	 * @return
	 */
	public long count(KeyValueQuery<?> query, String keyspace) {

		CRITERIA criteria = this.criteriaAccessor.map(it -> it.resolve(query)).orElse(null);
		return count(criteria, keyspace);
	}

	/**
	 * @param criteria
	 * @param sort
	 * @param offset
	 * @param rows
	 * @param keyspace
	 * @return
	 */
	public abstract Collection<?> execute(@Nullable CRITERIA criteria, @Nullable SORT sort, long offset, int rows,
			String keyspace);

	/**
	 * @param criteria
	 * @param sort
	 * @param offset
	 * @param rows
	 * @param keyspace
	 * @param type
	 * @return
	 * @since 1.1
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> execute(@Nullable CRITERIA criteria, @Nullable SORT sort, long offset, int rows,
			String keyspace, Class<T> type) {
		return (Collection<T>) execute(criteria, sort, offset, rows, keyspace);
	}

	/**
	 * @param criteria
	 * @param keyspace
	 * @return
	 */
	public abstract long count(@Nullable CRITERIA criteria, String keyspace);

	/**
	 * Get the {@link KeyValueAdapter} used.
	 *
	 * @return
	 */
	@Nullable
	protected ADAPTER getAdapter() {
		return this.adapter;
	}

	/**
	 * Get the required {@link KeyValueAdapter} used or throw {@link IllegalStateException} if the adapter is not set.
	 *
	 * @return the required {@link KeyValueAdapter}.
	 * @throws IllegalStateException if the adapter is not set.
	 */
	protected ADAPTER getRequiredAdapter() {

		ADAPTER adapter = getAdapter();

		if (adapter != null) {
			return adapter;
		}

		throw new IllegalStateException("Required KeyValueAdapter is not set");
	}

	/**
	 * @param adapter
	 */
	@SuppressWarnings("unchecked")
	public void registerAdapter(KeyValueAdapter adapter) {

		if (this.adapter == null) {
			this.adapter = (ADAPTER) adapter;
		} else {
			throw new IllegalArgumentException("Cannot register more than one adapter for this QueryEngine");
		}
	}
}
