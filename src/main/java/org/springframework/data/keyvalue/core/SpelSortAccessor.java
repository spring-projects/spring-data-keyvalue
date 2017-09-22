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

import java.util.Comparator;
import java.util.Optional;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

/**
 * {@link SortAccessor} implementation capable of creating {@link SpelPropertyComparator}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class SpelSortAccessor implements SortAccessor<Comparator<?>> {

	private final SpelExpressionParser parser;

	/**
	 * Creates a new {@link SpelSortAccessor} given {@link SpelExpressionParser}.
	 *
	 * @param parser must not be {@literal null}.
	 */
	public SpelSortAccessor(SpelExpressionParser parser) {

		Assert.notNull(parser, "SpelExpressionParser must not be null!");
		this.parser = parser;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.SortAccessor#resolve(org.springframework.data.keyvalue.core.query.KeyValueQuery)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Comparator<?> resolve(KeyValueQuery<?> query) {

		if (query.getSort().isUnsorted()) {
			return null;
		}

		Optional<Comparator<?>> comparator = Optional.empty();
		for (Order order : query.getSort()) {

			SpelPropertyComparator<Object> spelSort = new SpelPropertyComparator<>(order.getProperty(), parser);

			if (Direction.DESC.equals(order.getDirection())) {

				spelSort.desc();

				if (!NullHandling.NATIVE.equals(order.getNullHandling())) {
					spelSort = NullHandling.NULLS_FIRST.equals(order.getNullHandling()) ? spelSort.nullsFirst()
							: spelSort.nullsLast();
				}
			}

			if (!comparator.isPresent()) {
				comparator = Optional.of(spelSort);
			} else {

				SpelPropertyComparator<Object> spelSortToUse = spelSort;
				comparator = comparator.map(it -> it.thenComparing(spelSortToUse));
			}
		}

		return comparator.orElseThrow(
				() -> new IllegalStateException("No sort definitions have been added to this CompoundComparator to compare"));
	}
}
