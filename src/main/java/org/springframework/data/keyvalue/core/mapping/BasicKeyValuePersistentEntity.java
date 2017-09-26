/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link KeyValuePersistentEntity} implementation that adds specific meta-data such as the {@literal keySpace}..
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @param <T>
 */
public class BasicKeyValuePersistentEntity<T, P extends KeyValuePersistentProperty<P>>
		extends BasicPersistentEntity<T, P> implements KeyValuePersistentEntity<T, P> {

	private static final KeySpaceResolver DEFAULT_FALLBACK_RESOLVER = ClassNameKeySpaceResolver.INSTANCE;

	private final @Nullable String keyspace;

	/**
	 * @param information must not be {@literal null}.
	 * @param keySpaceResolver can be {@literal null}.
	 */
	public BasicKeyValuePersistentEntity(TypeInformation<T> information,
			@Nullable KeySpaceResolver fallbackKeySpaceResolver) {

		super(information);

		this.keyspace = detectKeySpace(information.getType(), fallbackKeySpaceResolver);
	}

	@Nullable
	private static String detectKeySpace(Class<?> type, @Nullable KeySpaceResolver fallback) {

		String keySpace = AnnotationBasedKeySpaceResolver.INSTANCE.resolveKeySpace(type);

		if (StringUtils.hasText(keySpace)) {
			return keySpace;
		}

		return (fallback == null ? DEFAULT_FALLBACK_RESOLVER : fallback).resolveKeySpace(type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity#getKeySpace()
	 */
	@Override
	public String getKeySpace() {
		return this.keyspace;
	}
}
