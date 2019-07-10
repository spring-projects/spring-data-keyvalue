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

import static org.assertj.core.api.Assertions.*;

import lombok.Data;

import java.util.AbstractMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.data.util.CloseableIterator;

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

	@Test // DATACMNS-525
	public void putShouldThrowExceptionWhenAddingNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.put(null, object1, COLLECTION_1));
	}

	@Test // DATACMNS-525
	public void putShouldThrowExceptionWhenCollectionIsNullValue() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.put("1", object1, null));
	}

	@Test // DATACMNS-525
	public void putReturnsNullWhenNoObjectForIdPresent() {
		assertThat(adapter.put("1", object1, COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	public void putShouldReturnPreviousObjectForIdWhenAddingNewOneWithSameIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.put("1", object2, COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATACMNS-525
	public void containsShouldThrowExceptionWhenIdIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.contains(null, COLLECTION_1));
	}

	@Test // DATACMNS-525
	public void containsShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.contains("", null));
	}

	@Test // DATACMNS-525
	public void containsShouldReturnFalseWhenNoElementsPresent() {
		assertThat(adapter.contains("1", COLLECTION_1)).isFalse();
	}

	@Test // DATACMNS-525
	public void containShouldReturnTrueWhenElementWithIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.contains("1", COLLECTION_1)).isTrue();
	}

	@Test // DATACMNS-525
	public void getShouldReturnNullWhenNoElementWithIdPresent() {
		assertThat(adapter.get("1", COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	public void getShouldReturnElementWhenMatchingIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.get("1", COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATACMNS-525
	public void getShouldThrowExceptionWhenIdIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.get(null, COLLECTION_1));
	}

	@Test // DATACMNS-525
	public void getShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.get("1", null));
	}

	@Test // DATACMNS-525
	public void getAllOfShouldReturnAllValuesOfGivenCollection() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);
		adapter.put("3", STRING_1, COLLECTION_2);

		assertThat((Iterable) adapter.getAllOf(COLLECTION_1)).contains(object1, object2);
	}

	@Test // DATACMNS-525
	public void getAllOfShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.getAllOf(null));
	}

	@Test // DATACMNS-525
	public void deleteShouldReturnNullWhenGivenIdThatDoesNotExist() {
		assertThat(adapter.delete("1", COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	public void deleteShouldReturnDeletedObject() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.delete("1", COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATAKV-99
	public void scanShouldIterateOverAvailableEntries() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);

		CloseableIterator<Map.Entry<Object, Object>> iterator = adapter.entries(COLLECTION_1);

		assertThat(iterator.next()).isEqualTo(new AbstractMap.SimpleEntry<>("1", object1));
		assertThat(iterator.next()).isEqualTo(new AbstractMap.SimpleEntry<>("2", object2));
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test // DATAKV-99
	public void scanShouldReturnEmptyIteratorWhenNoElementsAvailable() {
		assertThat(adapter.entries(COLLECTION_1).hasNext()).isFalse();
	}

	@Test // DATAKV-99
	public void scanDoesNotMixResultsFromMultipleKeyspaces() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_2);

		CloseableIterator<Map.Entry<Object, Object>> iterator = adapter.entries(COLLECTION_1);

		assertThat(iterator.next()).isEqualTo(new AbstractMap.SimpleEntry<>("1", object1));
		assertThat(iterator.hasNext()).isFalse();
	}

	@Data
	static class SimpleObject {

		protected String stringValue;

		public SimpleObject() {}

		SimpleObject(String value) {
			this.stringValue = value;
		}
	}

}
