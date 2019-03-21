/*
 * Copyright 2014-2019 the original author or authors.
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

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.springframework.data.keyvalue.test.util.IsEntry.*;

import java.io.Serializable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 */
public class MapKeyValueAdapterUnitTests {

	private static final String COLLECTION_1 = "collection-1";
	private static final String COLLECTION_2 = "collection-2";
	private static final String STRING_1 = new String("1");

	private Object object1 = new SimpleObject("one");
	private Object object2 = new SimpleObject("two");

	private MapKeyValueAdapter adapter;

	@Before
	public void setUp() {
		this.adapter = new MapKeyValueAdapter();
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void putShouldThrowExceptionWhenAddingNullId() {
		adapter.put(null, object1, COLLECTION_1);
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void putShouldThrowExceptionWhenCollectionIsNullValue() {
		adapter.put("1", object1, null);
	}

	@Test // DATACMNS-525
	public void putReturnsNullWhenNoObjectForIdPresent() {
		assertThat(adapter.put("1", object1, COLLECTION_1), nullValue());
	}

	@Test // DATACMNS-525
	public void putShouldReturnPreviousObjectForIdWhenAddingNewOneWithSameIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.put("1", object2, COLLECTION_1), equalTo(object1));
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void containsShouldThrowExceptionWhenIdIsNull() {
		adapter.contains(null, COLLECTION_1);
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void containsShouldThrowExceptionWhenTypeIsNull() {
		adapter.contains("", null);
	}

	@Test // DATACMNS-525
	public void containsShouldReturnFalseWhenNoElementsPresent() {
		assertThat(adapter.contains("1", COLLECTION_1), is(false));
	}

	@Test // DATACMNS-525
	public void containShouldReturnTrueWhenElementWithIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.contains("1", COLLECTION_1), is(true));
	}

	@Test // DATACMNS-525
	public void getShouldReturnNullWhenNoElementWithIdPresent() {
		assertThat(adapter.get("1", COLLECTION_1), nullValue());
	}

	@Test // DATACMNS-525
	public void getShouldReturnElementWhenMatchingIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.get("1", COLLECTION_1), is(object1));
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void getShouldThrowExceptionWhenIdIsNull() {
		adapter.get(null, COLLECTION_1);
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void getShouldThrowExceptionWhenTypeIsNull() {
		adapter.get("1", null);
	}

	@Test // DATACMNS-525
	public void getAllOfShouldReturnAllValuesOfGivenCollection() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);
		adapter.put("3", STRING_1, COLLECTION_2);

		assertThat(adapter.getAllOf(COLLECTION_1), containsInAnyOrder(object1, object2));
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525
	public void getAllOfShouldThrowExceptionWhenTypeIsNull() {
		adapter.getAllOf(null);
	}

	@Test // DATACMNS-525
	public void deleteShouldReturnNullWhenGivenIdThatDoesNotExist() {
		assertThat(adapter.delete("1", COLLECTION_1), nullValue());
	}

	@Test // DATACMNS-525
	public void deleteShouldReturnDeletedObject() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.delete("1", COLLECTION_1), is(object1));
	}

	@Test // DATAKV-99
	public void scanShouldIterateOverAvailableEntries() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);

		CloseableIterator<Map.Entry<Serializable, Object>> iterator = adapter.entries(COLLECTION_1);

		assertThat(iterator.next(), isEntry("1", object1));
		assertThat(iterator.next(), isEntry("2", object2));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test // DATAKV-99
	public void scanShouldReturnEmptyIteratorWhenNoElementsAvailable() {
		assertThat(adapter.entries(COLLECTION_1).hasNext(), is(false));
	}

	@Test // DATAKV-99
	public void scanDoesNotMixResultsFromMultipleKeyspaces() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_2);

		CloseableIterator<Map.Entry<Serializable, Object>> iterator = adapter.entries(COLLECTION_1);

		assertThat(iterator.next(), isEntry("1", object1));
		assertThat(iterator.hasNext(), is(false));
	}

	static class SimpleObject {

		protected String stringValue;

		public SimpleObject() {}

		SimpleObject(String value) {
			this.stringValue = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * ObjectUtils.nullSafeHashCode(this.stringValue);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof SimpleObject)) {
				return false;
			}
			SimpleObject that = (SimpleObject) obj;
			return ObjectUtils.nullSafeEquals(this.stringValue, that.stringValue);
		}
	}

}
