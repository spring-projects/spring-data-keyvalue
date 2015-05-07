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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Christoph Strobl
 * @param <K>
 * @param <V>
 */
public class ForwardingKeyValueIterator<K, V> implements KeyValueIterator<K, V> {

	private final Iterator<? extends Map.Entry<K, V>> delegate;

	public ForwardingKeyValueIterator(Iterator<? extends java.util.Map.Entry<K, V>> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public Entry<K, V> next() {
		return new ForwardingEntry(delegate.next());
	}

	@Override
	public void close() throws IOException {

	}

	class ForwardingEntry implements Entry<K, V> {

		private final Map.Entry<K, V> entry;

		public ForwardingEntry(Map.Entry<K, V> entry) {
			this.entry = entry;
		}

		@Override
		public K getKey() {
			return entry.getKey();
		}

		@Override
		public V getValue() {
			return entry.getValue();
		}

		@Override
		public V setValue(V value) {
			return entry.setValue(value);
		}

		@Override
		public String toString() {
			return entry != null ? entry.toString() : "null";
		}

	}
}
