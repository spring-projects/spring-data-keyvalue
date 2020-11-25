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
package org.springframework.data.map.repository.config;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Integration tests for {@link MapRepositoryConfigurationExtension}.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 */
class MapRepositoriesConfigurationExtensionIntegrationTests {

	@Test // DATAKV-86
	void registersDefaultTemplateIfReferenceNotCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

		assertThat(Arrays.asList(context.getBeanDefinitionNames())).contains("mapKeyValueTemplate");

		context.close();
	}

	@Test // DATAKV-86
	void doesNotRegisterDefaulttemplateIfReferenceIsCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				ConfigWithCustomTemplateReference.class);

		assertThat(context.getBeanDefinitionNames()).doesNotContain("mapKeyValueTemplate");

		context.close();
	}

	@Test // DATAKV-87
	void considersMapTypeConfiguredOnAnnotation() {
		assertKeyValueTemplateWithAdapterFor(ConcurrentSkipListMap.class,
				new AnnotationConfigApplicationContext(ConfigWithCustomizedMapType.class));
	}

	@Test // DATAKV-87
	void doesNotConsiderMapConfiguredIfTemplateIsPresent() {
		assertKeyValueTemplateWithAdapterFor(ConcurrentHashMap.class, new AnnotationConfigApplicationContext(
				ConfigWithCustomizedMapTypeAndExplicitDefinitionOfKeyValueTemplate.class));
	}

	private static void assertKeyValueTemplateWithAdapterFor(Class<?> mapType, ApplicationContext context) {

		KeyValueTemplate template = context.getBean(KeyValueTemplate.class);
		Object adapter = ReflectionTestUtils.getField(template, "adapter");

		assertThat(adapter).isInstanceOf(MapKeyValueAdapter.class);
		assertThat(ReflectionTestUtils.getField(adapter, "store")).isInstanceOf(mapType);
	}

	@Configuration
	@EnableMapRepositories
	static class Config {}

	@Configuration
	@EnableMapRepositories(keyValueTemplateRef = "foo")
	static class ConfigWithCustomTemplateReference {}

	@Configuration
	@EnableMapRepositories(mapType = ConcurrentSkipListMap.class)
	static class ConfigWithCustomizedMapType {}

	@Configuration
	@EnableMapRepositories(mapType = ConcurrentSkipListMap.class)
	static class ConfigWithCustomizedMapTypeAndExplicitDefinitionOfKeyValueTemplate {

		@Bean
		public KeyValueTemplate mapKeyValueTemplate() {
			return new KeyValueTemplate(new MapKeyValueAdapter());
		}
	}
}
