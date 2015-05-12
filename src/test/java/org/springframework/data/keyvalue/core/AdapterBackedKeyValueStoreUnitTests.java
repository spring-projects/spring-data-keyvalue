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

import static org.hamcrest.collection.IsEmptyIterable.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class AdapterBackedKeyValueStoreUnitTests<K extends Serializable, V> {

	@SuppressWarnings("unchecked") K key = (K) "key";
	@SuppressWarnings("unchecked") V value = (V) "value";

	final String KEYSPACE = "keyspace-1";
	@Mock KeyValueAdapter adapterMock;
	AdapterBackedKeyValueStore<K, V> store;

	@Before
	public void setUp() {
		store = new AdapterBackedKeyValueStore<K, V>(adapterMock, KEYSPACE);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test(expected = IllegalArgumentException.class)
	public void constructorShouldThrowExceptionWhenAdapterIsNull() {
		new AdapterBackedKeyValueStore<Serializable, V>(null, KEYSPACE);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test(expected = IllegalArgumentException.class)
	public void constructorShouldThrowExceptionWhenKeyspaceIsNull() {
		new AdapterBackedKeyValueStore<Serializable, V>(adapterMock, null);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void putShouldDelegateToAdapterCorrectly() {

		store.put(key, value);

		verify(adapterMock, times(1)).put(eq(key), eq(value), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void getShouldDelegateToAdapterCorrectly() {

		store.get(key);

		verify(adapterMock, times(1)).get(eq(key), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void containsShouldDelegateToAdapterCorrectly() {

		store.contains(key);

		verify(adapterMock, times(1)).contains(eq(key), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void getAllShouldDelegateToAdapterCorrectly() {

		store.getAll(Collections.singleton(key));

		verify(adapterMock, times(1)).get(eq(key), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void getAllShouldReturnEmptyCollectionWhenKeysCollectionIsEmpty() {

		assertThat(store.getAll(Collections.<K> emptySet()), emptyIterable());

		verifyZeroInteractions(adapterMock);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void getAllShouldReturnEmptyCollectionWhenKeysCollectionIsNull() {

		assertThat(store.getAll(null), emptyIterable());

		verifyZeroInteractions(adapterMock);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void removeShouldDelegateToAdapterCorrectly() {

		store.remove(key);

		verify(adapterMock, times(1)).delete(eq(key), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void removeAllShouldDelegateToAdapterCorrectly() {

		store.removeAll(Collections.singleton(key));

		verify(adapterMock, times(1)).delete(eq(key), eq(KEYSPACE));
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void removeAllShouldReturnEmptyCollectionWhenKeysCollectionIsEmpty() {

		assertThat(store.removeAll(Collections.<K> emptySet()), emptyIterable());

		verifyZeroInteractions(adapterMock);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void removeAllShouldReturnEmptyCollectionWhenKeysCollectionIsNull() {

		assertThat(store.removeAll(null), emptyIterable());

		verifyZeroInteractions(adapterMock);
	}

	/**
	 * @see DATAKV-103
	 */
	@Test
	public void clearShouldDelegateToAdapterCorrectly() {

		store.clear();

		verify(adapterMock, times(1)).deleteAllOf(eq(KEYSPACE));
	}

}
