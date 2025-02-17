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
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.lang.Contract;

/**
 * {@link Comparator} implementation to compare objects based on a {@link PropertyPath}. This comparator obtains the
 * value at {@link PropertyPath} from the {@link #compare(Object, Object) given comparison objects} and then performs
 * the comparison.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 3.1.10
 */
public class PropertyPathComparator<T> implements Comparator<T> {

	private static final Comparator<?> NULLS_FIRST = Comparator.nullsFirst(Comparator.naturalOrder());
	private static final Comparator<?> NULLS_LAST = Comparator.nullsLast(Comparator.naturalOrder());

	private final String path;

	private boolean asc = true;
	private boolean nullsFirst = true;

	private final Map<Class<?>, PropertyPath> pathCache = new HashMap<>(2);

	public PropertyPathComparator(String path) {
		this.path = path;
	}

	@Override
	public int compare(@Nullable T o1, @Nullable T o2) {

		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1 == null) {
			return nullsFirst ? 1 : -1;
		}
		if (o2 == null) {
			return nullsFirst ? 1 : -1;
		}

		PropertyPath propertyPath = pathCache.computeIfAbsent(o1.getClass(), it -> PropertyPath.from(path, it));
		Object value1 = getCompareValue(o1, propertyPath);
		Object value2 = getCompareValue(o2, propertyPath);

		return getComparator().compare(value1, value2) * (asc ? 1 : -1);
	}

	protected <S> @Nullable Object getCompareValue(S object, PropertyPath propertyPath) {
		return new SimplePropertyPathAccessor<>(object).getValue(propertyPath);
	}

	@SuppressWarnings("unchecked")
	private Comparator<@Nullable Object> getComparator() {
		return (Comparator<Object>) (nullsFirst ? NULLS_FIRST : NULLS_LAST);
	}

	/**
	 * Sort {@literal ascending}.
	 *
	 * @return
	 */
	@Contract("-> this")
	public PropertyPathComparator<@Nullable T> asc() {
		this.asc = true;
		return this;
	}

	/**
	 * Sort {@literal descending}.
	 *
	 * @return
	 */
	@Contract("-> this")
	public PropertyPathComparator<@Nullable T> desc() {
		this.asc = false;
		return this;
	}

	/**
	 * Sort {@literal null} values first.
	 *
	 * @return
	 */
	@Contract("-> this")
	public PropertyPathComparator<@Nullable T> nullsFirst() {
		this.nullsFirst = true;
		return this;
	}

	/**
	 * Sort {@literal null} values last.
	 *
	 * @return
	 */
	@Contract("-> this")
	public PropertyPathComparator<@Nullable T> nullsLast() {
		this.nullsFirst = false;
		return this;
	}
}
