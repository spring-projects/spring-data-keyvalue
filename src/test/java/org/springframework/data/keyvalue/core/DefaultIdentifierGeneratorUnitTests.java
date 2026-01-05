/*
 * Copyright 2016-present the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.TypeInformation;

/**
 * @author Christoph Strobl
 */
class DefaultIdentifierGeneratorUnitTests {

	private DefaultIdentifierGenerator generator = DefaultIdentifierGenerator.INSTANCE;

	@Test
	void shouldThrowExceptionForUnsupportedType() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
				.isThrownBy(() -> generator.generateIdentifierOfType(TypeInformation.of(Date.class)));
	}

	@Test // DATAKV-136
	void shouldGenerateUUIDValueCorrectly() {

		Object value = generator.generateIdentifierOfType(TypeInformation.of(UUID.class));

		assertThat(value).isNotNull().isInstanceOf(UUID.class);
	}

	@Test // DATAKV-136
	void shouldGenerateStringValueCorrectly() {

		Object value = generator.generateIdentifierOfType(TypeInformation.of(String.class));

		assertThat(value).isNotNull().isInstanceOf(String.class);
	}

	@Test // DATAKV-136
	void shouldGenerateLongValueCorrectly() {

		Object value = generator.generateIdentifierOfType(TypeInformation.of(Long.class));

		assertThat(value).isNotNull().isInstanceOf(Long.class);
	}

	@Test // DATAKV-136
	void shouldGenerateIntValueCorrectly() {

		Object value = generator.generateIdentifierOfType(TypeInformation.of(Integer.class));

		assertThat(value).isNotNull().isInstanceOf(Integer.class);
	}
}
