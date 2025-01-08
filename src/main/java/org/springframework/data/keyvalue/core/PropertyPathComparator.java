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

import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.util.Lazy;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * @author Christoph Strobl
 * @since 3.1.10
 */
public class PropertyPathComparator<T> implements Comparator<T> {

	private final String path;

	private boolean asc = true;
	private boolean nullsFirst = true;

	private final Map<Class<?>, PropertyPath> pathCache = new HashMap<>(2);
	private Lazy<Comparator<Object>> comparator = Lazy
			.of(() -> new NullSafeComparator(Comparator.naturalOrder(), this.nullsFirst));

	public PropertyPathComparator(String path) {
		this.path = path;
	}

	@Override
	public int compare(T o1, T o2) {

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
		Object value1 = new SimplePropertyPathAccessor<>(o1).getValue(propertyPath);
		Object value2 = new SimplePropertyPathAccessor<>(o2).getValue(propertyPath);

		return comparator.get().compare(value1, value2) * (asc ? 1 : -1);
	}

	/**
	 * Sort {@literal ascending}.
	 *
	 * @return
	 */
	public PropertyPathComparator<T> asc() {
		this.asc = true;
		return this;
	}

	/**
	 * Sort {@literal descending}.
	 *
	 * @return
	 */
	public PropertyPathComparator<T> desc() {
		this.asc = false;
		return this;
	}

	/**
	 * Sort {@literal null} values first.
	 *
	 * @return
	 */
	public PropertyPathComparator<T> nullsFirst() {
		this.nullsFirst = true;
		return this;
	}

	/**
	 * Sort {@literal null} values last.
	 *
	 * @return
	 */
	public PropertyPathComparator<T> nullsLast() {
		this.nullsFirst = false;
		return this;
	}
}
