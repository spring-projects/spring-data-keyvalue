/*
 * Copyright 2014-2025 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.QueryEngineFactory;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * {@link RepositoryConfigurationExtension} for Map-based repositories.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@SuppressWarnings("unchecked")
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

		SortAccessor<?> sortAccessor = getSortAccessor(configurationSource);
		QueryEngine<?, ?, ?> queryEngine = getQueryEngine(sortAccessor, configurationSource);

		if (queryEngine != null) {
			adapterBuilder.addConstructorArgValue(queryEngine);
		} else if (sortAccessor != null) {
			adapterBuilder.addConstructorArgValue(sortAccessor);
		}

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(KeyValueTemplate.class);
		builder
				.addConstructorArgValue(ParsingUtils.getSourceBeanDefinition(adapterBuilder, configurationSource.getSource()));
		builder.setRole(BeanDefinition.ROLE_SUPPORT);

		return ParsingUtils.getSourceBeanDefinition(builder, configurationSource.getSource());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Class<? extends Map> getMapTypeToUse(RepositoryConfigurationSource source) {

		return (Class<? extends Map>) getAnnotationAttributes(source).get("mapType");
	}

	@Nullable
	private static SortAccessor<?> getSortAccessor(RepositoryConfigurationSource source) {

		Class<? extends SortAccessor<?>> sortAccessorType = (Class<? extends SortAccessor<?>>) getAnnotationAttributes(
				source).get("sortAccessor");

		if (sortAccessorType != null && !sortAccessorType.isInterface()) {
			return BeanUtils.instantiateClass(sortAccessorType);
		}

		return null;
	}

	@Nullable
	private static QueryEngine<?, ?, ?> getQueryEngine(@Nullable SortAccessor<?> sortAccessor,
			RepositoryConfigurationSource source) {

		Class<? extends QueryEngineFactory> queryEngineFactoryType = (Class<? extends QueryEngineFactory>) getAnnotationAttributes(
				source).get("queryEngineFactory");

		if(queryEngineFactoryType == null || queryEngineFactoryType.isInterface()) {
			return null;
		}

		if (sortAccessor != null) {
			Constructor<? extends QueryEngineFactory> constructor = ClassUtils
					.getConstructorIfAvailable(queryEngineFactoryType, SortAccessor.class);
			if (constructor != null) {
				return BeanUtils.instantiateClass(constructor, sortAccessor).create();
			}
		}

		return BeanUtils.instantiateClass(queryEngineFactoryType).create();
	}

	private static Map<String, Object> getAnnotationAttributes(RepositoryConfigurationSource source) {

		AnnotationMetadata annotationSource = (AnnotationMetadata) source.getSource();

		if (annotationSource == null) {
			throw new IllegalArgumentException("AnnotationSource not available");
		}

		Map<String, Object> annotationAttributes = annotationSource
				.getAnnotationAttributes(EnableMapRepositories.class.getName());

		if (annotationAttributes == null) {
			throw new IllegalStateException("No annotation attributes for @EnableMapRepositories");
		}

		return annotationAttributes;
	}
}
