/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.data.keyvalue.test.util;

import java.util.AbstractMap;
import java.util.Map;

import org.hamcrest.CustomMatcher;
import org.hamcrest.core.IsEqual;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 */
public class IsEntry extends CustomMatcher<Map.Entry<?, ?>> {

	private final Map.Entry<?, ?> expected;

	private IsEntry(Map.Entry<?, ?> entry) {
		super(String.format("an entry %s=%s.", entry != null ? entry.getKey() : "null", entry != null ? entry.getValue()
				: "null"));
		this.expected = entry;
	}

	@Override
	public boolean matches(Object item) {

		if (item == null && expected == null) {
			return true;
		}

		if (!(item instanceof Map.Entry)) {
			return false;
		}

		Map.Entry<?, ?> actual = (Map.Entry<?, ?>) item;

		return new IsEqual<Object>(expected.getKey()).matches(actual.getKey())
				&& new IsEqual<Object>(expected.getValue()).matches(actual.getValue());
	}

	public static IsEntry isEntry(Object key, Object value) {
		return isEntry(new EntryImpl(key, value));
	}

	public static IsEntry isEntry(Map.Entry<?, ?> entry) {
		return new IsEntry(entry);
	}

	private static class EntryImpl extends AbstractMap.SimpleEntry<Object, Object> {

		private static final long serialVersionUID = 1L;

		private EntryImpl(Object key, Object value) {
			super(key, value);
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}
	}
}
