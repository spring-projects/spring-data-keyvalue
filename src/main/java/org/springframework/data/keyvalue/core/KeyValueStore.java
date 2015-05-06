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

import java.io.Serializable;

/**
 * @author Christoph Strobl
 */
public class KeyValueStore<K extends Serializable, V> {

	private final KeyValueAccessor accessor;
	private final Serializable keyspace;

	public KeyValueStore(KeyValueAccessor accessor, Serializable keyspace) {
		this.accessor = accessor;
		this.keyspace = keyspace;
	}

	@SuppressWarnings("unchecked")
	public V put(K id, V item) {
		return (V) accessor.put(id, item, keyspace);
	}

	public boolean contains(K id) {
		return accessor.contains(id, keyspace);
	}

	@SuppressWarnings("unchecked")
	public V get(K id) {
		return (V) accessor.get(id, keyspace);
	}

	@SuppressWarnings("unchecked")
	public V delete(K id) {
		return (V) accessor.delete(id, keyspace);
	}

	public long count() {
		return accessor.count(null, keyspace);
	}

}
