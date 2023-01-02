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
package org.springframework.data.map.repository.config;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

/**
 * @author Christoph Strobl
 */
public class MapRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {

	@Override
	public String getModuleName() {
		return "Map";
	}

	@Override
	protected String getModulePrefix() {
		return "map";
	}

	@Override
	protected String getDefaultKeyValueTemplateRef() {
		return "mapKeyValueTemplate";
	}

	@Override
	protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
			RepositoryConfigurationSource configurationSource) {

		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.rootBeanDefinition(MapKeyValueAdapter.class);
		adapterBuilder.addConstructorArgValue(getMapTypeToUse(configurationSource));

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(KeyValueTemplate.class);
		builder
				.addConstructorArgValue(ParsingUtils.getSourceBeanDefinition(adapterBuilder, configurationSource.getSource()));
		builder.setRole(BeanDefinition.ROLE_SUPPORT);

		return ParsingUtils.getSourceBeanDefinition(builder, configurationSource.getSource());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Class<? extends Map> getMapTypeToUse(RepositoryConfigurationSource source) {

		return (Class<? extends Map>) ((AnnotationMetadata) source.getSource()).getAnnotationAttributes(
				EnableMapRepositories.class.getName()).get("mapType");
	}
}
