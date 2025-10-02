/*
 * Copyright 2025 the original author or authors.
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

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Keyspace store that uses a map of maps to store keyspace data. The outer map holds the keyspaces and the inner maps
 * hold the actual keys and values.
 *
 * @param store reference to the map of maps holding the keyspace data.
 * @param keySpaceMapType map type to be used for each keyspace map.
 * @param initialCapacity initial keyspace map capacity to optimize allocations.
 * @since 4.0
 */
@SuppressWarnings("rawtypes")
record MapKeySpaceStore(Map<String, Map<Object, Object>> store, Class<? extends Map> keySpaceMapType,
		int initialCapacity) implements KeySpaceStore {

	public static final int DEFAULT_INITIAL_CAPACITY = 1000;

	/**
	 * Create a new {@link KeySpaceStore} using {@link ConcurrentHashMap} as backing map type for each keyspace map.
	 *
	 * @return a new and empty {@link KeySpaceStore}.
	 */
	public static KeySpaceStore create() {
		return new MapKeySpaceStore(new ConcurrentHashMap<>(100), ConcurrentHashMap.class, DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * Create new {@link KeySpaceStore} using given map type for each keyspace map.
	 *
	 * @param mapType map type to use.
	 * @return the new {@link KeySpaceStore} object.
	 */
	public static KeySpaceStore of(Class<? extends Map> mapType) {

		Assert.notNull(mapType, "Store map type must not be null");

		return of(CollectionFactory.createMap(mapType, 100));
	}

	/**
	 * Create new {@link KeySpaceStore} using given map as backing store. Determines the map type from the given map.
	 *
	 * @param store map of maps.
	 * @return the new {@link KeySpaceStore} object for the given {@code store}.
	 */
	@SuppressWarnings("unchecked")
	public static KeySpaceStore of(Map<String, Map<Object, Object>> store) {

		Assert.notNull(store, "Store map must not be null");

		Class<? extends Map<?, ?>> userClass = (Class<? extends Map<?, ?>>) ClassUtils.getUserClass(store);
		return new MapKeySpaceStore(store, userClass, DEFAULT_INITIAL_CAPACITY);
	}

	@Override
	public Map<Object, Object> getKeySpace(String keyspace) {
		return store.computeIfAbsent(keyspace, k -> CollectionFactory.createMap(keySpaceMapType, initialCapacity));
	}

	@Override
	public void clear() {

		store.values().forEach(Map::clear);
		store.clear();
	}

}
