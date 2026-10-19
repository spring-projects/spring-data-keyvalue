/*
 * Copyright 2014-present the original author or authors.
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

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.TypeInformation;
import org.springframework.util.ClassUtils;

/**
 * Default implementation of {@link IdentifierGenerator} to generate identifiers of types {@link UUID}, String,
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
enum DefaultIdentifierGenerator implements IdentifierGenerator {

	INSTANCE;

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	@SuppressWarnings("unchecked")
	public <T> T generateIdentifierOfType(TypeInformation<T> identifierType) {

		Class<?> type = identifierType.getType();

		if (ClassUtils.isAssignable(UUID.class, type)) {
			return (T) UUID.randomUUID();
		} else if (ClassUtils.isAssignable(String.class, type)) {
			return (T) UUID.randomUUID().toString();
		} else if (ClassUtils.isAssignable(Integer.class, type)) {
			return (T) Integer.valueOf(this.secureRandom.nextInt());
		} else if (ClassUtils.isAssignable(Long.class, type)) {
			return (T) Long.valueOf(this.secureRandom.nextLong());
		}

		throw new InvalidDataAccessApiUsageException(
				String.format("Identifier cannot be generated for %s; Supported types are: UUID, String, Integer, and Long",
						identifierType.getType().getName()));
	}
}
