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
package org.springframework.data.keyvalue.test.util;

import org.hamcrest.CustomMatcher;
import org.hamcrest.core.IsEqual;
import org.springframework.data.keyvalue.core.Entry;

/**
 * @author Christoph Strobl
 */
public class IsEntry extends CustomMatcher<Entry<?, ?>> {

	private final Entry<?, ?> expected;

	private IsEntry(Entry<?, ?> entry) {
		super(String.format("an entry %s=%s.", entry != null ? entry.getKey() : "null", entry != null ? entry.getValue()
				: "null"));
		this.expected = entry;
	}

	@Override
	public boolean matches(Object item) {

		if (item == null && expected == null) {
			return true;
		}

		if (!(item instanceof Entry)) {
			return false;
		}

		Entry<?, ?> actual = (Entry<?, ?>) item;

		return new IsEqual<Object>(expected.getKey()).matches(actual.getKey())
				&& new IsEqual<Object>(expected.getValue()).matches(actual.getValue());
	}

	public static IsEntry isEntry(Object key, Object value) {
		return isEntry(new EntryImpl(key, value));
	}

	public static IsEntry isEntry(Entry<?, ?> entry) {
		return new IsEntry(entry);
	}

	private static class EntryImpl implements Entry<Object, Object> {

		private final Object key;
		private final Object value;

		private EntryImpl(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}
	}
}
