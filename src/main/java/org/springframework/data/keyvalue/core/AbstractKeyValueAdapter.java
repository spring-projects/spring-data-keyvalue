/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.Collection;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * Base implementation of {@link KeyValueAdapter} holds {@link QueryEngine} to delegate {@literal find} and
 * {@literal count} execution to.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public abstract class AbstractKeyValueAdapter implements KeyValueAdapter {

	private final QueryEngine<? extends KeyValueAdapter, ?, ?> engine;

	/**
	 * Creates new {@link AbstractKeyValueAdapter} with using the default query engine.
	 */
	protected AbstractKeyValueAdapter() {
		this(null);
	}

	/**
	 * Creates new {@link AbstractKeyValueAdapter} with using the default query engine.
	 *
	 * @param engine will be defaulted to {@link SpelQueryEngine} if {@literal null}.
	 */
	protected AbstractKeyValueAdapter(QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {

		this.engine = engine != null ? engine : new SpelQueryEngine();
		this.engine.registerAdapter(this);
	}

	/**
	 * Get the {@link QueryEngine} used.
	 *
	 * @return
	 */
	protected QueryEngine<? extends KeyValueAdapter, ?, ?> getQueryEngine() {
		return engine;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#get(java.lang.Object, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T get(Object id, String keyspace, Class<T> type) {
		return type.cast(get(id, keyspace));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#delete(java.lang.Object, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T delete(Object id, String keyspace, Class<T> type) {
		return type.cast(delete(id, keyspace));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#find(org.springframework.data.keyvalue.core.query.KeyValueQuery, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type) {
		return engine.execute(query, keyspace, type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#find(org.springframework.data.keyvalue.core.query.KeyValueQuery, java.lang.String)
	 */
	@Override
	public Collection<?> find(KeyValueQuery<?> query, String keyspace) {
		return engine.execute(query, keyspace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeyValueAdapter#count(org.springframework.data.keyvalue.core.query.KeyValueQuery, java.lang.String)
	 */
	@Override
	public long count(KeyValueQuery<?> query, String keyspace) {
		return engine.count(query, keyspace);
	}
}
