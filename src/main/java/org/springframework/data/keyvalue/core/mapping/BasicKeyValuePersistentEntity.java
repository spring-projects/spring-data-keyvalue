/*
 * Copyright 2015 the original author or authors.
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

import org.springframework.data.keyvalue.core.KeySpaceResolver;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link KeyValuePersistentEntity} implementation that adds specific meta-data such as the {@literal keySpace}..
 * 
 * @author Christoph Strobl
 * @param <T>
 */
public class BasicKeyValuePersistentEntity<T> extends BasicPersistentEntity<T, KeyValuePersistentProperty> implements
		KeyValuePersistentEntity<T> {

	private final String keyspace;

	/**
	 * @param information must not be {@literal null}.
	 * @param keySpaceResolver must not be {@literal null}.
	 */
	public BasicKeyValuePersistentEntity(TypeInformation<T> information, KeySpaceResolver fallbackKeySpaceResolver) {

		super(information);

		Assert.notNull(fallbackKeySpaceResolver, "FallbackKeySpaceResolver must not be null!");

		String resolvedKeySpace = AnnotationBasedKeySpaceResolver.INSTANCE.resolveKeySpace(information.getType());
		this.keyspace = StringUtils.hasText(resolvedKeySpace) ? resolvedKeySpace : fallbackKeySpaceResolver
				.resolveKeySpace(information.getType());

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
