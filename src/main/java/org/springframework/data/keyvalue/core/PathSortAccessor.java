/*
 * Copyright 2024-2025 the original author or authors.
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

import java.util.Comparator;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * @author Christoph Strobl
 * @since 3.1.10
 */
public class PathSortAccessor implements SortAccessor<Comparator<?>> {

	@Override
	public @Nullable Comparator<?> resolve(KeyValueQuery<?> query) {

		if (query.getSort().isUnsorted()) {
			return null;
		}

		Optional<Comparator<?>> comparator = Optional.empty();
		for (Order order : query.getSort()) {

			PropertyPathComparator<Object> pathSort = new PropertyPathComparator<>(order.getProperty());

			if (Direction.DESC.equals(order.getDirection())) {

				pathSort.desc();

				if (!NullHandling.NATIVE.equals(order.getNullHandling())) {
					pathSort = NullHandling.NULLS_FIRST.equals(order.getNullHandling()) ? pathSort.nullsFirst()
							: pathSort.nullsLast();
				}
			}

			if (!comparator.isPresent()) {
				comparator = Optional.of(pathSort);
			} else {

				PropertyPathComparator<Object> pathSortToUse = pathSort;
				comparator = comparator.map(it -> it.thenComparing(pathSortToUse));
			}
		}

		return comparator.orElseThrow(
				() -> new IllegalStateException("No sort definitions have been added to this CompoundComparator to compare"));

	}
}
