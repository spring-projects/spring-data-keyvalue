/*
 * Copyright 2014-2021 the original author or authors.
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
package org.springframework.data.keyvalue.core.query;

import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Marcel Overdijk
 * @param <T> Criteria type
 */
public class KeyValueQuery<T> {

	private Sort sort = Sort.unsorted();
	private long offset = -1;
	private int rows = -1;
	private final @Nullable T criteria;

	/**
	 * Creates new instance of {@link KeyValueQuery}.
	 */
	public KeyValueQuery() {
		this((T) null);
	}

	/**
	 * Creates new instance of {@link KeyValueQuery} with given criteria.
	 *
	 * @param criteria can be {@literal null}.
	 */
	public KeyValueQuery(@Nullable T criteria) {
		this.criteria = criteria;
	}

	/**
	 * Creates new instance of {@link KeyValueQuery} with given criteria and {@link Sort}.
	 *
	 * @param criteria can be {@literal null}.
	 * @param sort must not be {@literal null}.
	 * @since 2.4
	 */
	public KeyValueQuery(@Nullable T criteria, Sort sort) {
		this.criteria = criteria;
		setSort(sort);
	}

	/**
	 * Creates new instance of {@link KeyValueQuery} with given {@link Sort}.
	 *
	 * @param sort must not be {@literal null}.
	 */
	public KeyValueQuery(Sort sort) {
		this();
		setSort(sort);
	}

	/**
	 * Get the criteria object.
	 *
	 * @return
	 * @since 2.0
	 */
	@Nullable
	public T getCriteria() {
		return criteria;
	}

	/**
	 * Get {@link Sort}.
	 *
	 * @return
	 */
	public Sort getSort() {
		return sort;
	}

	/**
	 * Number of elements to skip.
	 *
	 * @return negative value if not set.
	 */
	public long getOffset() {
		return this.offset;
	}

	/**
	 * Number of elements to read.
	 *
	 * @return negative value if not set.
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * Set the number of elements to skip.
	 *
	 * @param offset use negative value for none.
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Set the number of elements to read.
	 *
	 * @param rows use negative value for all.
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Set {@link Sort} to be applied.
	 *
	 * @param sort
	 */
	public void setSort(Sort sort) {

		Assert.notNull(sort, "Sort must not be null!");

		this.sort = sort;
	}

	/**
	 * Add given {@link Sort}.
	 *
	 * @param sort must not be {@literal null}.
	 * @return
	 */
	public KeyValueQuery<T> orderBy(Sort sort) {

		Assert.notNull(sort, "Sort must not be null!");

		if (this.sort.isSorted()) {
			this.sort = this.sort.and(sort);
		} else {
			this.sort = sort;
		}

		return this;
	}

	/**
	 * @see KeyValueQuery#setOffset(long)
	 * @param offset
	 * @return
	 */
	public KeyValueQuery<T> skip(long offset) {

		setOffset(offset);

		return this;
	}

	/**
	 * @see KeyValueQuery#setRows(int)
	 * @param rows
	 * @return
	 */
	public KeyValueQuery<T> limit(int rows) {
		setRows(rows);
		return this;
	}

}
