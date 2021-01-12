/*
 * Copyright 2014-2021 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping.context;

import org.springframework.data.keyvalue.core.mapping.BasicKeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

/**
 * Default implementation of a {@link MappingContext} using {@link KeyValuePersistentEntity} and
 * {@link KeyValuePersistentProperty} as primary abstractions.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueMappingContext<E extends KeyValuePersistentEntity<?, P>, P extends KeyValuePersistentProperty<P>>
		extends AbstractMappingContext<E, P> {

	private @Nullable KeySpaceResolver fallbackKeySpaceResolver;

	/**
	 * Configures the {@link KeySpaceResolver} to be used if not explicit key space is annotated to the domain type.
	 *
	 * @param fallbackKeySpaceResolver can be {@literal null}.
	 */
	public void setFallbackKeySpaceResolver(KeySpaceResolver fallbackKeySpaceResolver) {
		this.fallbackKeySpaceResolver = fallbackKeySpaceResolver;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> E createPersistentEntity(TypeInformation<T> typeInformation) {
		return (E) new BasicKeyValuePersistentEntity<T, P>(typeInformation, fallbackKeySpaceResolver);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected P createPersistentProperty(Property property, E owner, SimpleTypeHolder simpleTypeHolder) {
		return (P) new KeyValuePersistentProperty<>(property, owner, simpleTypeHolder);
	}
}
