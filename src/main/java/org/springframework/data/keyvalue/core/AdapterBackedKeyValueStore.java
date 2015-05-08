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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * A {@link KeyValueStore} scoped to a keyspace that stores the values in via a provided {@link KeyValueAdapter}. 
 * 
 * @author Christoph Strobl
 * @param <K>
 * @param <V>
 */
public class AdapterBackedKeyValueStore<K extends Serializable, V> implements KeyValueStore<K, V> {

	private final Serializable keyspace;
	private final KeyValueAdapter adapter;

	/**
	 * @param adapter
	 * @param keyspace
	 */
	public AdapterBackedKeyValueStore(KeyValueAdapter adapter, Serializable keyspace) {

		Assert.notNull(adapter, "Adapter must not be null!");
		Assert.notNull(keyspace, "Keyspace must not be null!");
		this.adapter = adapter;
		this.keyspace = keyspace;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#put(java.io.Serializable, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V put(K id, V item) {
		return (V) adapter.put(id, item, keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(K id) {
		return adapter.contains(id, keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#get(java.io.Serializable)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V get(K id) {
		return (V) adapter.get(id, keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#getAll(java.util.Collection)
	 */
	@Override
	public Iterable<V> getAll(Collection<K> keys) {

		if (CollectionUtils.isEmpty(keys)) {
			return Collections.emptySet();
		}

		List<V> result = new ArrayList<V>(keys.size());
		for (K key : keys) {
			result.add(get(key));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#delete(java.io.Serializable)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V remove(K id) {
		return (V) adapter.delete(id, keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#removeAll(java.util.Collection)
	 */
	@Override
	public Iterable<V> removeAll(Collection<K> keys) {

		if (CollectionUtils.isEmpty(keys)) {
			return Collections.emptySet();
		}

		List<V> result = new ArrayList<V>(keys.size());
		for (K key : keys) {
			result.add(remove(key));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#size()
	 */
	@Override
	public long size() {
		return adapter.count(keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueStore#clear()
	 */
	@Override
	public void clear() {
		adapter.deleteAllOf(keyspace);
	}

}
