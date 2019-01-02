/*
 * Copyright 2014-2019 the original author or authors.
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

import org.springframework.data.keyvalue.core.mapping.BasicKeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * Default implementation of a {@link MappingContext} using {@link KeyValuePersistentEntity} and
 * {@link KeyValuePersistentProperty} as primary abstractions.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class KeyValueMappingContext extends
		AbstractMappingContext<KeyValuePersistentEntity<?>, KeyValuePersistentProperty> {

	private KeySpaceResolver fallbackKeySpaceResolver;

	/**
	 * Configures the {@link KeySpaceResolver} to be used if not explicit key space is annotated to the domain type.
	 * 
	 * @param fallbackKeySpaceResolver can be {@literal null}.
	 */
	public void setFallbackKeySpaceResolver(KeySpaceResolver fallbackKeySpaceResolver) {
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
