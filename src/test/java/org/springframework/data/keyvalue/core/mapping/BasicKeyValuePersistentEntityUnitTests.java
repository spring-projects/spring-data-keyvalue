/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.spel.ExtensionAwareEvaluationContextProvider;
import org.springframework.data.spel.spi.EvaluationContextExtension;

/**
 * Unit tests for {@link BasicKeyValuePersistentEntity}.
 *
 * @author Mark Paluch
 */
public class BasicKeyValuePersistentEntityUnitTests {

	MappingContext<? extends KeyValuePersistentEntity<?, ?>, ? extends KeyValuePersistentProperty<?>> mappingContext = new KeyValueMappingContext<>();

	@Test // DATAKV-268
	public void shouldDeriveKeyspaceFromClassName() {

		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(KeyspaceEntity.class);

		assertThat(persistentEntity.getKeySpace()).isEqualTo(KeyspaceEntity.class.getName());
	}

	@Test // DATAKV-268
	public void shouldEvaluateKeyspaceExpression() {

		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(ExpressionEntity.class);

		persistentEntity.setEvaluationContextProvider(
				new ExtensionAwareEvaluationContextProvider(Collections.singletonList(new SampleExtension())));

		assertThat(persistentEntity.getKeySpace()).isEqualTo("some");
	}

	@KeySpace("#{myProperty}")
	static class ExpressionEntity {}

	@KeySpace
	static class KeyspaceEntity {}

	static class SampleExtension implements EvaluationContextExtension {

		@Override
		public String getExtensionId() {
			return "sampleExtension";
		}

		@Override
		public Map<String, Object> getProperties() {

			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("myProperty", "some");
			return properties;
		}
	}
}
