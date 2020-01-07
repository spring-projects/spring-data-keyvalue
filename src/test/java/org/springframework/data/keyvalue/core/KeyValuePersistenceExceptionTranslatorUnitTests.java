/*
 * Copyright 2014-2020 the original author or authors.
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

import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author Christoph Strobl
 */
public class KeyValuePersistenceExceptionTranslatorUnitTests {

	KeyValuePersistenceExceptionTranslator translator = new KeyValuePersistenceExceptionTranslator();

	@Test // DATACMNS-525
	public void translateExeptionShouldReturnDataAccessExceptionWhenGivenOne() {
		assertThat(translator.translateExceptionIfPossible(new DataRetrievalFailureException("booh")),
				instanceOf(DataRetrievalFailureException.class));
	}

	@Test(expected = IllegalArgumentException.class) // DATACMNS-525, DATAKV-192
	public void translateExeptionShouldReturnNullWhenGivenNull() {
		assertThat(translator.translateExceptionIfPossible(null), nullValue());
	}

	@Test // DATACMNS-525
	public void translateExeptionShouldTranslateNoSuchElementExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new NoSuchElementException("")),
				instanceOf(DataRetrievalFailureException.class));
	}

	@Test // DATACMNS-525
	public void translateExeptionShouldTranslateIndexOutOfBoundsExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new IndexOutOfBoundsException("")),
				instanceOf(DataRetrievalFailureException.class));
	}

	@Test // DATACMNS-525
	public void translateExeptionShouldTranslateIllegalStateExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new IllegalStateException("")),
				instanceOf(DataRetrievalFailureException.class));
	}

	@Test // DATACMNS-525
	public void translateExeptionShouldTranslateAnyJavaExceptionToUncategorizedKeyValueException() {
		assertThat(translator.translateExceptionIfPossible(new UnsupportedOperationException("")),
				instanceOf(UncategorizedKeyValueException.class));
	}

	@Test // DATACMNS-525
	public void translateExeptionShouldReturnNullForNonJavaExceptions() {
		assertThat(translator.translateExceptionIfPossible(new NoSuchBeanDefinitionException("")), nullValue());
	}

}
