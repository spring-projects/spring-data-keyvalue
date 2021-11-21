/*
 * Copyright 2014-2021 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.util.CloseableIterator;

/**
 * @author Christoph Strobl
 * @author Hang Xie
 */
class MapKeyValueAdapterUnitTests {

	private static final String COLLECTION_1 = "collection-1";
	private static final String COLLECTION_2 = "collection-2";
	private static final String STRING_1 = new String("1");

	private Object object1 = new SimpleObject("one");
	private Object object2 = new SimpleObject("two");

	private MapKeyValueAdapter adapter;

	@BeforeEach
	void setUp() {
		this.adapter = new MapKeyValueAdapter();
	}

	@Test // DATACMNS-525
	void putShouldThrowExceptionWhenAddingNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.put(null, object1, COLLECTION_1));
	}

	@Test // DATACMNS-525
	void putShouldThrowExceptionWhenCollectionIsNullValue() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.put("1", object1, null));
	}

	@Test // DATACMNS-525
	void putReturnsNullWhenNoObjectForIdPresent() {
		assertThat(adapter.put("1", object1, COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	void putShouldReturnPreviousObjectForIdWhenAddingNewOneWithSameIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.put("1", object2, COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATACMNS-525
	void containsShouldThrowExceptionWhenIdIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.contains(null, COLLECTION_1));
	}

	@Test // DATACMNS-525
	void containsShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.contains("", null));
	}

	@Test // DATACMNS-525
	void containsShouldReturnFalseWhenNoElementsPresent() {
		assertThat(adapter.contains("1", COLLECTION_1)).isFalse();
	}

	@Test // DATACMNS-525
	void containShouldReturnTrueWhenElementWithIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.contains("1", COLLECTION_1)).isTrue();
	}

	@Test // DATACMNS-525
	void getShouldReturnNullWhenNoElementWithIdPresent() {
		assertThat(adapter.get("1", COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	void getShouldReturnElementWhenMatchingIdPresent() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.get("1", COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATACMNS-525
	void getShouldThrowExceptionWhenIdIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.get(null, COLLECTION_1));
	}

	@Test // DATACMNS-525
	void getShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.get("1", null));
	}

	@Test // DATACMNS-525
	void getAllOfShouldReturnAllValuesOfGivenCollection() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);
		adapter.put("3", STRING_1, COLLECTION_2);

		assertThat((Iterable) adapter.getAllOf(COLLECTION_1)).contains(object1, object2);
	}

	@Test // DATACMNS-525
	void getAllOfShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> adapter.getAllOf(null));
	}

	@Test // DATACMNS-525
	void deleteShouldReturnNullWhenGivenIdThatDoesNotExist() {
		assertThat(adapter.delete("1", COLLECTION_1)).isNull();
	}

	@Test // DATACMNS-525
	void deleteShouldReturnDeletedObject() {

		adapter.put("1", object1, COLLECTION_1);
		assertThat(adapter.delete("1", COLLECTION_1)).isEqualTo(object1);
	}

	@Test // DATAKV-99
	void scanShouldIterateOverAvailableEntries() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_1);

		CloseableIterator<Map.Entry<Object, Object>> iterator = adapter.entries(COLLECTION_1);

		Map.Entry<Object, Object> entry1 = iterator.next();
		Map.Entry<Object, Object> entry2 = iterator.next();

		if(entry1.getKey().equals("1")) {
			assertThat(entry1).isEqualTo(new AbstractMap.SimpleEntry<>("1", object1));
			assertThat(entry2).isEqualTo(new AbstractMap.SimpleEntry<>("2", object2));
		} else {
			assertThat(entry2).isEqualTo(new AbstractMap.SimpleEntry<>("2", object2));
			assertThat(entry1).isEqualTo(new AbstractMap.SimpleEntry<>("1", object1));
		}

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test // DATAKV-99
	void scanShouldReturnEmptyIteratorWhenNoElementsAvailable() {
		assertThat(adapter.entries(COLLECTION_1).hasNext()).isFalse();
	}

	@Test // DATAKV-99
	void scanDoesNotMixResultsFromMultipleKeyspaces() {

		adapter.put("1", object1, COLLECTION_1);
		adapter.put("2", object2, COLLECTION_2);

		CloseableIterator<Map.Entry<Object, Object>> iterator = adapter.entries(COLLECTION_1);

		Map.Entry<Object, Object> entry1 = iterator.next();

		if(entry1.getKey().equals("1")) {
			assertThat(entry1).isEqualTo(new AbstractMap.SimpleEntry<>("1", object1));
		} else {
			assertThat(entry1).isEqualTo(new AbstractMap.SimpleEntry<>("2", object2));
		}

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
