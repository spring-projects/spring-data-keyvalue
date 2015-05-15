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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingKeySpaceResolverUnitTests {

	@Mock KeySpaceResolver delegateMock;
	CachingKeySpaceResolver resolver;

	@Before
	public void setUp() {

		when(delegateMock.resolveKeySpace(eq(String.class))).thenReturn("foo");
		when(delegateMock.resolveKeySpace(eq(Integer.class))).thenReturn("bar");

		this.resolver = new CachingKeySpaceResolver(delegateMock);
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void resolveKeySpaceShouldBeDelegatedCorrectly() {

		resolver.resolveKeySpace(String.class);

		verify(delegateMock, times(1)).resolveKeySpace(String.class);
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void resolveKeySpaceShouldBeCachedCorrectly() {

		resolver.resolveKeySpace(String.class);
		resolver.resolveKeySpace(String.class);

		verify(delegateMock, times(1)).resolveKeySpace(String.class);
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void resolveKeySpaceShouldBeCachedPerType() {

		resolver.resolveKeySpace(String.class);
		resolver.resolveKeySpace(Integer.class);

		verify(delegateMock, times(1)).resolveKeySpace(String.class);
		verify(delegateMock, times(1)).resolveKeySpace(Integer.class);
	}
}
