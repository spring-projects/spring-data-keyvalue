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

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Integration tests for {@link MapRepositoryConfigurationExtension}.
 * 
 * @author Oliver Gierke
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

	@Configuration
	@EnableMapRepositories
	static class Config {}

	@Configuration
	@EnableMapRepositories(keyValueTemplateRef = "foo")
	static class ConfigWithCustomTemplateReference {}
}
