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

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class ForwardingIteratorUnitTests<K, V> {

	@Mock Iterator<Map.Entry<K, V>> iteratorMock;

	/**
	 * @see DATAKV-99
	 */
	@Test
	public void hasNextShoudDelegateToWrappedIterator() {

		when(iteratorMock.hasNext()).thenReturn(true);

		assertThat(new ForwardingKeyValueIterator<K, V>(iteratorMock).hasNext(), is(true));

		verify(iteratorMock, times(1)).hasNext();
	}

	/**
	 * @see DATAKV-99
	 */
	@Test
	public void nextShoudDelegateToWrappedIterator() {

		when(iteratorMock.next()).thenReturn((Map.Entry<K, V>) mock(Map.Entry.class));

		assertThat(new ForwardingKeyValueIterator<K, V>(iteratorMock).next(), notNullValue());

		verify(iteratorMock, times(1)).next();
	}

	/**
	 * @see DATAKV-99
	 */
	@Test(expected = NoSuchElementException.class)
	public void nextShoudThrowErrorWhenWrappedIteratorHasNoMoreElements() {

		when(iteratorMock.next()).thenThrow(new NoSuchElementException());

		new ForwardingKeyValueIterator<K, V>(iteratorMock).next();
	}

	/**
	 * @see DATAKV-99
	 */
	@Test
	public void closeShouldDoNothing() throws IOException {

		new ForwardingKeyValueIterator<K, V>(iteratorMock).close();

		verifyZeroInteractions(iteratorMock);
	}

}
