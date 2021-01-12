/*
 * Copyright 2016-2021 the original author or authors.
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

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.util.ClassTypeInformation;

/**
 * @author Christoph Strobl
 */
public class DefaultIdentifierGeneratorUnitTests {

	DefaultIdentifierGenerator generator = DefaultIdentifierGenerator.INSTANCE;

	@Test
	public void shouldThrowExceptionForUnsupportedType() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
				.isThrownBy(() -> generator.generateIdentifierOfType(ClassTypeInformation.from(Date.class)));
	}

	@Test // DATAKV-136
	public void shouldGenerateUUIDValueCorrectly() {

		Object value = generator.generateIdentifierOfType(ClassTypeInformation.from(UUID.class));

		assertThat(value).isNotNull();
		assertThat(value).isInstanceOf(UUID.class);
	}

	@Test // DATAKV-136
	public void shouldGenerateStringValueCorrectly() {

		Object value = generator.generateIdentifierOfType(ClassTypeInformation.from(String.class));

		assertThat(value).isNotNull();
		assertThat(value).isInstanceOf(String.class);
	}

	@Test // DATAKV-136
	public void shouldGenerateLongValueCorrectly() {

		Object value = generator.generateIdentifierOfType(ClassTypeInformation.from(Long.class));

		assertThat(value).isNotNull();
		assertThat(value).isInstanceOf(Long.class);
	}

	@Test // DATAKV-136
	public void shouldGenerateIntValueCorrectly() {

		Object value = generator.generateIdentifierOfType(ClassTypeInformation.from(Integer.class));

		assertThat(value).isNotNull();
		assertThat(value).isInstanceOf(Integer.class);
	}
}
