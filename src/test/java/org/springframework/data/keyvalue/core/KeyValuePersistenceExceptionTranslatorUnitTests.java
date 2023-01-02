/*
 * Copyright 2014-2023 the original author or authors.
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

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author Christoph Strobl
 */
class KeyValuePersistenceExceptionTranslatorUnitTests {

	private KeyValuePersistenceExceptionTranslator translator = new KeyValuePersistenceExceptionTranslator();

	@Test // DATACMNS-525
	void translateExeptionShouldReturnDataAccessExceptionWhenGivenOne() {
		assertThat(translator.translateExceptionIfPossible(new DataRetrievalFailureException("booh")))
				.isInstanceOf(DataRetrievalFailureException.class);
	}

	@Test // DATACMNS-525, DATAKV-192
	void translateExeptionShouldReturnNullWhenGivenNull() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> assertThat(translator.translateExceptionIfPossible(null)).isNull());
	}

	@Test // DATACMNS-525
	void translateExeptionShouldTranslateNoSuchElementExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new NoSuchElementException("")))
				.isInstanceOf(DataRetrievalFailureException.class);
	}

	@Test // DATACMNS-525
	void translateExeptionShouldTranslateIndexOutOfBoundsExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new IndexOutOfBoundsException("")))
				.isInstanceOf(DataRetrievalFailureException.class);
	}

	@Test // DATACMNS-525
	void translateExeptionShouldTranslateIllegalStateExceptionToDataRetrievalFailureException() {
		assertThat(translator.translateExceptionIfPossible(new IllegalStateException("")))
				.isInstanceOf(DataRetrievalFailureException.class);
	}

	@Test // DATACMNS-525
	void translateExeptionShouldTranslateAnyJavaExceptionToUncategorizedKeyValueException() {
		assertThat(translator.translateExceptionIfPossible(new UnsupportedOperationException("")))
				.isInstanceOf(UncategorizedKeyValueException.class);
	}

	@Test // DATACMNS-525
	void translateExeptionShouldReturnNullForNonJavaExceptions() {
		assertThat(translator.translateExceptionIfPossible(new NoSuchBeanDefinitionException(""))).isNull();
	}

}
