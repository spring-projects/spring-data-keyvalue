/*
 * Copyright 2022-2025 the original author or authors.
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
package org.springframework.data.keyvalue.aot;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;

/**
 * {@link RuntimeHintsRegistrar} for KeyValue.
 *
 * @author Christoph Strobl
 * @since 3.0
 */
class KeyValueRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {

		// REFLECTION
		hints.reflection().registerTypes(
				Arrays.asList(
						TypeReference.of(org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository.class),
						TypeReference.of(KeyValuePartTreeQuery.class)),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));

		hints.reflection().registerType(TypeReference.of("java.util.Comparators.NaturalOrderComparator"),
				builder -> builder.withMethod("compare",
						List.of(TypeReference.of(Object.class), TypeReference.of(Object.class)), ExecutableMode.INVOKE));

		hints.reflection().registerType(TypeReference.of("java.util.Comparators.NullComparator"),
				builder -> builder.withMethod("compare",
						List.of(TypeReference.of(Object.class), TypeReference.of(Object.class)), ExecutableMode.INVOKE));
	}
}
