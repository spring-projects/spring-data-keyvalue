/*
 * Copyright 2015-2021 the original author or authors.
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
package org.springframework.data.keyvalue.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.util.CloseableIterator;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class ForwardingCloseableIteratorUnitTests<K, V> {

	@Mock Iterator<Entry<K, V>> iteratorMock;
	@Mock Runnable closeActionMock;

	@Test // DATAKV-99
	void hasNextShouldDelegateToWrappedIterator() {

		when(iteratorMock.hasNext()).thenReturn(true);

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			assertThat(iterator.hasNext()).isTrue();
			verify(iteratorMock, times(1)).hasNext();
		} finally {
			iterator.close();
		}
	}

	@Test // DATAKV-99
	@SuppressWarnings("unchecked")
	void nextShouldDelegateToWrappedIterator() {

		when(iteratorMock.next()).thenReturn((Entry<K, V>) mock(Map.Entry.class));

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			assertThat(iterator.next()).isNotNull();
			verify(iteratorMock, times(1)).next();
		} finally {
			iterator.close();
		}
	}

	@Test // DATAKV-99
	void nextShouldThrowErrorWhenWrappedIteratorHasNoMoreElements() {

		when(iteratorMock.next()).thenThrow(new NoSuchElementException());

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
		} finally {
			iterator.close();
		}
	}

	@Test // DATAKV-99
	void closeShouldDoNothingByDefault() {

		new ForwardingCloseableIterator<>(iteratorMock).close();

		verifyZeroInteractions(iteratorMock);
	}

	@Test // DATAKV-99
	void closeShouldInvokeConfiguredCloseAction() {

		new ForwardingCloseableIterator<>(iteratorMock, closeActionMock).close();

		verify(closeActionMock, times(1)).run();
	}
}
