/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.map.repository.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

/**
 * Integration tests for {@link MapRepositoryConfigurationExtension}.
 * 
 * @author Oliver Gierke
 * @author Christoph Strobl
 */
public class MapRepositoriesConfigurationExtensionIntegrationTests {

	/**
	 * @see DATAKV-86
	 */
	@Test
	public void registersDefaultTemplateIfReferenceNotCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

		assertThat(Arrays.asList(context.getBeanDefinitionNames()), hasItem("mapKeyValueTemplate"));

		context.close();
	}

	/**
	 * @see DATAKV-86
	 */
	@Test
	public void doesNotRegisterDefaulttemplateIfReferenceIsCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				ConfigWithCustomTemplateReference.class);

		assertThat(Arrays.asList(context.getBeanDefinitionNames()), not(hasItem("mapKeyValueTemplate")));

		context.close();
	}

	/**
	 * @see DATAKV-87
	 */
	@Test
	public void registeresMapKeyValueAdapterFactoryWithDefaultMapTypeWhenIsNotCostomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

		assertThat(context.getBeanFactory().getBeanDefinition("mapKeyValueAdapterFactory").getConstructorArgumentValues()
				.getGenericArgumentValue(Class.class).getValue().equals(ConcurrentHashMap.class), is(true));

		context.close();
	}

	/**
	 * @see DATAKV-87
	 */
	@Test
	public void registeresMapKeyValueAdapterFactoryWithGivenMapTypeWhenIsCostomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ConfigWithCustomizedMapType.class);

		assertThat(context.getBeanFactory().getBeanDefinition("mapKeyValueAdapterFactory").getConstructorArgumentValues()
				.getGenericArgumentValue(Class.class).getValue().equals(ConcurrentSkipListMap.class), is(true));

		context.close();
	}

	/**
	 * @see DATAKV-87
	 */
	@Test
	public void doesNotRegisterMapKeyValueAdapterFactoryWhenKeyValueTemplateIsCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				ConfigWithCustomizedMapTypeAndExplicitDefinitionOfKeyValueTemplate.class);

		assertThat(Arrays.asList(context.getBeanDefinitionNames()), not(hasItem("mapKeyValueAdapterFactory")));

		context.close();
	}

	/**
	 * @see DATAKV-87
	 */
	@Test
	public void doesNotRegisterMapKeyValueAdapterFactoryWhenTemplateReferenceIsCustomized() {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				ConfigWithCustomTemplateReference.class);

		assertThat(Arrays.asList(context.getBeanDefinitionNames()), not(hasItem("mapKeyValueAdapterFactory")));

		context.close();
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
			return new KeyValueTemplate(Mockito.mock(KeyValueAdapter.class));
		}
	}

}
