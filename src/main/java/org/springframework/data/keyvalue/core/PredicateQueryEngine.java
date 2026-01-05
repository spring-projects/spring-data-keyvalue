/*
 * Copyright 2024-present the original author or authors.
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
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.lang.Nullable;

/**
 * {@link QueryEngine} implementation specific for executing {@link Predicate} based {@link KeyValueQuery} against
 * {@link KeyValueAdapter}.
 *
 * @author Christoph Strobl
 * @since 3.3
 */
public class PredicateQueryEngine extends QueryEngine<KeyValueAdapter, Predicate<?>, Comparator<?>> {

	/**
	 * Creates a new {@link PredicateQueryEngine}.
	 */
	public PredicateQueryEngine() {
		this(new PathSortAccessor());
	}

	/**
	 * Creates a new query engine using provided {@link SortAccessor accessor} for sorting results.
	 */
	public PredicateQueryEngine(SortAccessor<Comparator<?>> sortAccessor) {
		super(new CriteriaAccessor<>() {
			@Nullable
			@Override
			public Predicate<?> resolve(KeyValueQuery<?> query) {
				return (Predicate<?>) query.getCriteria();
			}
		}, sortAccessor);
	}

	@Override
	public Collection<?> execute(@Nullable Predicate<?> criteria, @Nullable Comparator<?> sort, long offset, int rows,
			String keyspace) {
		return sortAndFilterMatchingRange(getRequiredAdapter().getAllOf(keyspace), criteria, sort, offset, rows);
	}

	@Override
	public long count(@Nullable Predicate<?> criteria, String keyspace) {
		return filterMatchingRange(IterableConverter.toList(getRequiredAdapter().getAllOf(keyspace)), criteria, -1, -1)
				.size();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> sortAndFilterMatchingRange(Iterable<?> source, @Nullable Predicate<?> criteria,
			@Nullable Comparator sort, long offset, int rows) {

		List<?> tmp = IterableConverter.toList(source);
		if (sort != null) {
			tmp.sort(sort);
		}

		return filterMatchingRange(tmp, criteria, offset, rows);
	}

	private static <S> List<S> filterMatchingRange(List<S> source, @Nullable Predicate criteria, long offset, int rows) {

		Stream<S> stream = source.stream();

		if (criteria != null) {
			stream = stream.filter(criteria);
		}
		if (offset > 0) {
			stream = stream.skip(offset);
		}
		if (rows > 0) {
			stream = stream.limit(rows);
		}

		return stream.collect(Collectors.toList());
	}
}
