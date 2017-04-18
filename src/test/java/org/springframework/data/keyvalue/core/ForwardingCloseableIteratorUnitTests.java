/*
 * Copyright 2015-2017 the original author or authors.
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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.util.CloseableIterator;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class ForwardingCloseableIteratorUnitTests<K, V> {

	@Mock Iterator<Entry<K, V>> iteratorMock;
	@Mock Runnable closeActionMock;

	@Test // DATAKV-99
	public void hasNextShoudDelegateToWrappedIterator() {

		when(iteratorMock.hasNext()).thenReturn(true);

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			assertThat(iterator.hasNext(), is(true));
			verify(iteratorMock, times(1)).hasNext();
		} finally {
			iterator.close();
		}
	}

	@Test // DATAKV-99
	@SuppressWarnings("unchecked")
	public void nextShoudDelegateToWrappedIterator() {

		when(iteratorMock.next()).thenReturn((Entry<K, V>) mock(Map.Entry.class));

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			assertThat(iterator.next(), notNullValue());
			verify(iteratorMock, times(1)).next();
		} finally {
			iterator.close();
		}
	}

	@Test(expected = NoSuchElementException.class) // DATAKV-99
	public void nextShoudThrowErrorWhenWrappedIteratorHasNoMoreElements() {

		when(iteratorMock.next()).thenThrow(new NoSuchElementException());

		CloseableIterator<Entry<K, V>> iterator = new ForwardingCloseableIterator<>(iteratorMock);

		try {
			iterator.next();
		} finally {
			iterator.close();
		}
	}

	@Test // DATAKV-99
	public void closeShouldDoNothingByDefault() {

		new ForwardingCloseableIterator<>(iteratorMock).close();

		verifyZeroInteractions(iteratorMock);
	}

	@Test // DATAKV-99
	public void closeShouldInvokeConfiguredCloseAction() {

		new ForwardingCloseableIterator<>(iteratorMock, closeActionMock).close();

		verify(closeActionMock, times(1)).run();
	}
}
