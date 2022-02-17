/*
 * Copyright 2014-2022 the original author or authors.
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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.CollectionFactory;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link KeyValueAdapter} implementation for {@link Map}.
 *
 * @author Christoph Strobl
 * @author Derek Cochran
 * @author Marcel Overdijk
 */
public class MapKeyValueAdapter extends AbstractKeyValueAdapter {

	@SuppressWarnings("rawtypes") //
	private final Class<? extends Map> keySpaceMapType;
	private final Map<String, Map<Object, Object>> store;

	/**
	 * Create new {@link MapKeyValueAdapter} using {@link ConcurrentHashMap} as backing store type.
	 */
	public MapKeyValueAdapter() {
		this(ConcurrentHashMap.class);
	}

	/**
	 * Create new {@link MapKeyValueAdapter} using the given query engine.
	 *
	 * @param engine the query engine.
	 * @since 2.4
	 */
	public MapKeyValueAdapter(QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
		this(ConcurrentHashMap.class, engine);
	}

	/**
	 * Creates a new {@link MapKeyValueAdapter} using the given {@link Map} as backing store.
	 *
	 * @param mapType must not be {@literal null}.
	 */
	@SuppressWarnings("rawtypes")
	public MapKeyValueAdapter(Class<? extends Map> mapType) {
		this(CollectionFactory.createMap(mapType, 100), mapType, null);
	}

	/**
	 * Creates a new {@link MapKeyValueAdapter} using the given {@link Map} as backing store and query engine.
	 *
	 * @param mapType must not be {@literal null}.
	 * @param engine the query engine.
	 * @since 2.4
	 */
	@SuppressWarnings("rawtypes")
	public MapKeyValueAdapter(Class<? extends Map> mapType, QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
		this(CollectionFactory.createMap(mapType, 100), mapType, engine);
	}

	/**
	 * Create new instance of {@link MapKeyValueAdapter} using given dataStore for persistence.
	 *
	 * @param store must not be {@literal null}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MapKeyValueAdapter(Map<String, Map<Object, Object>> store) {
		this(store, (Class<? extends Map>) ClassUtils.getUserClass(store), null);
	}

	/**
	 * Create new instance of {@link MapKeyValueAdapter} using given dataStore for persistence and query engine.
	 *
	 * @param store must not be {@literal null}.
	 * @param engine the query engine.
	 * @since 2.4
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MapKeyValueAdapter(Map<String, Map<Object, Object>> store, QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
		this(store, (Class<? extends Map>) ClassUtils.getUserClass(store), engine);
	}

	/**
	 * Creates a new {@link MapKeyValueAdapter} with the given store and type to be used when creating key spaces and
	 * query engine.
	 *
	 * @param store must not be {@literal null}.
	 * @param keySpaceMapType must not be {@literal null}.
	 * @param engine the query engine.
	 */
	@SuppressWarnings("rawtypes")
	private MapKeyValueAdapter(Map<String, Map<Object, Object>> store, Class<? extends Map> keySpaceMapType, QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {

		super(engine);

		Assert.notNull(store, "Store must not be null.");
		Assert.notNull(keySpaceMapType, "Map type to be used for key spaces must not be null!");

		this.store = store;
		this.keySpaceMapType = keySpaceMapType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#put(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object put(Object id, Object item, String keyspace) {

		Assert.notNull(id, "Cannot add item with null id.");
		Assert.notNull(keyspace, "Cannot add item for null collection.");

		return getKeySpaceMap(keyspace).put(id, item);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#contains(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean contains(Object id, String keyspace) {
		return get(id, keyspace) != null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#count(java.lang.String)
	 */
	@Override
	public long count(String keyspace) {
		return getKeySpaceMap(keyspace).size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#get(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object get(Object id, String keyspace) {

		Assert.notNull(id, "Cannot get item with null id.");
		return getKeySpaceMap(keyspace).get(id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#delete(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object delete(Object id, String keyspace) {

		Assert.notNull(id, "Cannot delete item with null id.");
		return getKeySpaceMap(keyspace).remove(id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#getAllOf(java.lang.String)
	 */
	@Override
	public Collection<?> getAllOf(String keyspace) {
		return getKeySpaceMap(keyspace).values();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#entries(java.lang.String)
	 */
	@Override
	public CloseableIterator<Entry<Object, Object>> entries(String keyspace) {
		return new ForwardingCloseableIterator<>(getKeySpaceMap(keyspace).entrySet().iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#deleteAllOf(java.lang.String)
	 */
	@Override
	public void deleteAllOf(String keyspace) {
		getKeySpaceMap(keyspace).clear();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#clear()
	 */
	@Override
	public void clear() {
		store.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		clear();
	}

	/**
	 * Get map associated with given key space.
	 *
	 * @param keyspace must not be {@literal null}.
	 * @return
	 */
	protected Map<Object, Object> getKeySpaceMap(String keyspace) {

		Assert.notNull(keyspace, "Collection must not be null for lookup!");
		return store.computeIfAbsent(keyspace, k -> CollectionFactory.createMap(keySpaceMapType,  1000));
	}

}
