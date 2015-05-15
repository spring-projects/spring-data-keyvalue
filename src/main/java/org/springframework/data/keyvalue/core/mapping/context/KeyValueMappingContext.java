/*
 * Copyright 2014-2015 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping.context;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.springframework.data.keyvalue.core.ClassNameKeySpaceResolver;
import org.springframework.data.keyvalue.core.KeySpaceResolver;
import org.springframework.data.keyvalue.core.mapping.BasicKeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

/**
 * Default implementation of a {@link MappingContext} using {@link KeyValuePersistentEntity} and
 * {@link KeyValuePersistentProperty} as primary abstractions.
 * 
 * @author Christoph Strobl
 */
public class KeyValueMappingContext extends
		AbstractMappingContext<KeyValuePersistentEntity<?>, KeyValuePersistentProperty> {

	private final KeySpaceResolver fallbackKeySpaceResolver;

	/**
	 * Creates new {@link KeyValueMappingContext} using an {@link ClassNameKeySpaceResolver} for {@literal keyspace}
	 * resolution.
	 */
	public KeyValueMappingContext() {
		this(ClassNameKeySpaceResolver.INSTANCE);
	}

	/**
	 * Creates new {@link KeyValueMappingContext} using given {@link KeySpaceResolver}
	 * 
	 * @param keySpaceResolver must not be {@literal null}.
	 */
	public KeyValueMappingContext(KeySpaceResolver fallbackKeySpaceResolver) {

		Assert.notNull(fallbackKeySpaceResolver, "FallbackKeySpaceResolver must not be null!");
		this.fallbackKeySpaceResolver = fallbackKeySpaceResolver;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentEntity(org.springframework.data.util.TypeInformation)
	 */
	@Override
	protected <T> KeyValuePersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
		return new BasicKeyValuePersistentEntity<T>(typeInformation, fallbackKeySpaceResolver);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentProperty(java.lang.reflect.Field, java.beans.PropertyDescriptor, org.springframework.data.mapping.model.MutablePersistentEntity, org.springframework.data.mapping.model.SimpleTypeHolder)
	 */
	@Override
	protected KeyValuePersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor,
			KeyValuePersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
		return new KeyValuePersistentProperty(field, descriptor, owner, simpleTypeHolder);
	}
}
