/*
 * Copyright 2015-2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;

/**
 * Converter capable of transforming a given {@link Iterable} into a collection type.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public final class IterableConverter {

	private IterableConverter() {}

	/**
	 * Converts a given {@link Iterable} into a {@link List}
	 *
	 * @param source
	 * @return {@link Collections#emptyList()} when source is {@literal null}.
	 */
	@Contract("_ -> !null")
	public static <T> List<T> toList(@Nullable Iterable<T> source) {

		if(source == null) {
			return Collections.emptyList();
		}

		if (source instanceof List) {
			return (List<T>) source;
		}

		if (source instanceof Collection) {
			return new ArrayList<>((Collection<T>) source);
		}

		List<T> result = new ArrayList<>();
		for (T value : source) {
			result.add(value);
		}
		return result;
	}
}
