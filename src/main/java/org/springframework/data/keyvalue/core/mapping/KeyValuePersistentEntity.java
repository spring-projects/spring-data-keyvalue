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

import org.springframework.data.mapping.model.MutablePersistentEntity;
import org.springframework.lang.Nullable;

/**
 * @author Christoph Strobl
 * @param <T>
 */
public interface KeyValuePersistentEntity<T, P extends KeyValuePersistentProperty<P>>
		extends MutablePersistentEntity<T, P> {

	/**
	 * Get the {@literal keySpace} a given entity assigns to.
	 * 
	 * @return can be {@literal null}.
	 */
	@Nullable
	String getKeySpace();
}
