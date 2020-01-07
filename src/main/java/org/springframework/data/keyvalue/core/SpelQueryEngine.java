/*
 * Copyright 2014-2020 the original author or authors.
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;

/**
 * {@link QueryEngine} implementation specific for executing {@link SpelExpression} based {@link KeyValueQuery} against
 * {@link KeyValueAdapter}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @param <T>
 */
class SpelQueryEngine extends QueryEngine<KeyValueAdapter, SpelCriteria, Comparator<?>> {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	/**
	 * Creates a new {@link SpelQueryEngine}.
	 */
	public SpelQueryEngine() {
		super(new SpelCriteriaAccessor(PARSER), new SpelSortAccessor(PARSER));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.QueryEngine#execute(java.lang.Object, java.lang.Object, int, int, java.lang.String)
	 */
	@Override
	public Collection<?> execute(@Nullable SpelCriteria criteria, @Nullable Comparator<?> sort, long offset, int rows, String keyspace) {
		return sortAndFilterMatchingRange(getRequiredAdapter().getAllOf(keyspace), criteria, sort, offset, rows);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.QueryEngine#count(java.lang.Object, java.lang.String)
	 */
	@Override
	public long count(@Nullable SpelCriteria criteria, String keyspace) {
		return filterMatchingRange(IterableConverter.toList(getRequiredAdapter().getAllOf(keyspace)), criteria, -1, -1)
				.size();
	}

	@SuppressWarnings("unchecked")
	private List<?> sortAndFilterMatchingRange(Iterable<?> source, @Nullable SpelCriteria criteria, @Nullable Comparator sort,
			long offset, int rows) {

		List<?> tmp = IterableConverter.toList(source);
		if (sort != null) {
			tmp.sort(sort);
		}

		return filterMatchingRange(tmp, criteria, offset, rows);
	}

	private static <S> List<S> filterMatchingRange(List<S> source, @Nullable SpelCriteria criteria, long offset,
			int rows) {

		Stream<S> stream = source.stream();

		if (criteria != null) {
			stream = stream.filter(it -> evaluateExpression(criteria, it));
		}
		if (offset > 0) {
			stream = stream.skip(offset);
		}
		if (rows > 0) {
			stream = stream.limit(rows);
		}

		return stream.collect(Collectors.toList());
	}

	@SuppressWarnings("ConstantConditions")
	private static boolean evaluateExpression(SpelCriteria criteria, Object candidate) {

		try {
			return criteria.getExpression().getValue(criteria.getContext(), candidate, Boolean.class);
		} catch (SpelEvaluationException e) {
			criteria.getContext().setVariable("it", candidate);
			return criteria.getExpression().getValue(criteria.getContext()) == null ? false
					: criteria.getExpression().getValue(criteria.getContext(), Boolean.class);
		}
	}
}
