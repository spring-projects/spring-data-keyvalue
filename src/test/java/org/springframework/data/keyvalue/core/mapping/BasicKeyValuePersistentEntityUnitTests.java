/*
 * Copyright 2019-2025 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.spel.ExtensionAwareEvaluationContextProvider;
import org.springframework.data.spel.spi.EvaluationContextExtension;
import org.springframework.mock.env.MockEnvironment;

/**
 * Unit tests for {@link BasicKeyValuePersistentEntity}.
 *
 * @author Mark Paluch
 */
class BasicKeyValuePersistentEntityUnitTests {

	KeyValueMappingContext<? extends KeyValuePersistentEntity<?, ?>, ? extends KeyValuePersistentProperty<?>> mappingContext = new KeyValueMappingContext<>();

	@Test // DATAKV-268
	void shouldDeriveKeyspaceFromClassName() {

		assertThat(mappingContext.getPersistentEntity(KeyspaceEntity.class).getRequiredKeySpace())
				.isEqualTo(KeyspaceEntity.class.getName());
	}

	@Test // DATAKV-268, GH-613
	void shouldEvaluateKeyspaceExpression() {

		MockEnvironment environment = new MockEnvironment().withProperty("my.property", "foo");
		mappingContext.setEnvironment(environment);

		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(ExpressionEntity.class);
		persistentEntity.setEvaluationContextProvider(
				new ExtensionAwareEvaluationContextProvider(Collections.singletonList(new SampleExtension())));

		assertThat(persistentEntity.getRequiredKeySpace()).isEqualTo("some_foo");
	}

	@Test // DATAKV-268
	void shouldEvaluateEntityWithoutKeyspace() {

		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(NoKeyspaceEntity.class);
		persistentEntity.setEvaluationContextProvider(
				new ExtensionAwareEvaluationContextProvider(Collections.singletonList(new SampleExtension())));

		assertThat(persistentEntity.getRequiredKeySpace()).isEqualTo(NoKeyspaceEntity.class.getName());
	}

	@Test // GH-461
	void shouldApplyKeySpaceResolver() {

		mappingContext.setKeySpaceResolver(new PrefixKeyspaceResolver("foo", ClassNameKeySpaceResolver.INSTANCE));
		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(NoKeyspaceEntity.class);

		assertThat(persistentEntity.getKeySpace()).isEqualTo("foo" + NoKeyspaceEntity.class.getName());
	}

	@Test // GH-461
	void shouldFallBackToDefaultsIfKeySpaceResolverReturnsNull() {

		mappingContext.setKeySpaceResolver(it -> null);
		KeyValuePersistentEntity<?, ?> persistentEntity = mappingContext.getPersistentEntity(NoKeyspaceEntity.class);

		assertThat(persistentEntity.getKeySpace()).isEqualTo(NoKeyspaceEntity.class.getName());
	}

	@KeySpace("#{myProperty}_${my.property}")
	private static class ExpressionEntity {}

	@KeySpace
	private static class KeyspaceEntity {}

	private static class NoKeyspaceEntity {}

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
