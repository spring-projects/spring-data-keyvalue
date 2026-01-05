/*
 * Copyright 2025-present the original author or authors.
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
package org.springframework.data.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy interface to obtain a map for a given key space. Implementations should be thread-safe when intended for use
 * with multiple threads (both, the store itself and the used keystore maps).
 * <p>
 * Can be used to plug in keystore creation or implementation strategies (for example, Map-based implementations such as
 * MapDB or Infinispan) through a consolidated interface. A keyspace store represents a map of maps or a database with
 * multiple collections and can use any kind of map per keyspace.
 * <p>
 * For example, a {@link ConcurrentHashMap} can be used as keystore map type to allow concurrent access to keyspaces
 * using:
 *
 * <pre class="code">
 * KeyspaceStore store = KeyspaceStore.create();
 * </pre>
 *
 * Custom map types (or instances of these) can be used as well using the provided factory methods:
 *
 * <pre class="code">
 * KeyspaceStore store = KeyspaceStore.of(LinkedHashMap.class);
 *
 * Map<String, Map<Object, Object>> backingMap = …;
 * KeyspaceStore store = KeyspaceStore.of(backingMap);
 * </pre>
 *
 * @since 4.0
 */
public interface KeySpaceStore {

	/**
	 * Return the map associated with given keyspace. Implementations can return an empty map if the keyspace does not
	 * exist yet or a reference to the map that represents an existing keyspace holding keys and values for the requested
	 * keyspace.
	 *
	 * @param keyspace name of the keyspace to obtain the map for, must not be {@literal null}.
	 * @return the map associated with the given keyspace, never {@literal null}.
	 */
	Map<Object, Object> getKeySpace(String keyspace);

	/**
	 * Clear all keyspaces. Access to {@link #getKeySpace(String)} will return an empty map for each keyspace after this
	 * method call. It is not required to clear each keyspace individually but it makes sense to do so to free up memory.
	 */
	void clear();

	/**
	 * Create a new {@link KeySpaceStore} using {@link ConcurrentHashMap} as backing map type for each keyspace map.
	 *
	 * @return a new and empty {@link KeySpaceStore}.
	 */
	static KeySpaceStore create() {
		return MapKeySpaceStore.create();
	}

	/**
	 * Create new {@link KeySpaceStore} using given map type for each keyspace map.
	 *
	 * @param mapType map type to use.
	 * @return the new {@link KeySpaceStore} object.
	 */
	@SuppressWarnings("rawtypes")
	static KeySpaceStore of(Class<? extends Map> mapType) {
		return MapKeySpaceStore.of(mapType);
	}

	/**
	 * Create new {@link KeySpaceStore} using given map as backing store. Determines the map type from the given map.
	 *
	 * @param store map of maps.
	 * @return the new {@link KeySpaceStore} object for the given {@code store}.
	 */
	static KeySpaceStore of(Map<String, Map<Object, Object>> store) {
		return MapKeySpaceStore.of(store);
	}

}
