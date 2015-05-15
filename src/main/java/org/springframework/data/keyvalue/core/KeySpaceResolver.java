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
package org.springframework.data.keyvalue.core;

/**
 * {@link KeySpaceResolver} determines the {@literal keyspace} a given type is assigned to. A keyspace in this context
 * is a specific region/collection/grouping of elements sharing a common keyrange. <br />
 * 
 * @author Christoph Strobl
 */
public interface KeySpaceResolver {

	/**
	 * Determine the {@literal keySpace} to use for a given type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	String resolveKeySpace(Class<?> type);

	/**
	 * Get the value of the fallback {@literal keySpace}.
	 * 
	 * @param type
	 * @return
	 */
	String getFallbackKeySpace(Class<?> type);

}
