/*
 * Copyright 2014-present the original author or authors.
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
import java.util.Optional;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.QueryEngineFactory;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.map.KeySpaceStore;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
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
		adapterBuilder.addConstructorArgValue(getKeySpaceStore(configurationSource));

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

	private static Object getKeySpaceStore(RepositoryConfigurationSource source) {

		Optional<String> keySpaceStoreRef = source.getAttribute("keySpaceStoreRef", String.class);

		return keySpaceStoreRef.map(beanName -> new RuntimeBeanReference(beanName, KeySpaceStore.class)) //
				.map(Object.class::cast) //
				.orElseGet(() -> source.getRequiredAttribute("mapType", Class.class));
	}

	private static @Nullable SortAccessor<?> getSortAccessor(RepositoryConfigurationSource source) {

		Class<? extends SortAccessor<?>> sortAccessorType = getClassAttribute(source, "sortAccessor");

		if (sortAccessorType == null) {
			return null;
		}

		return BeanUtils.instantiateClass(sortAccessorType);
	}

	private static @Nullable QueryEngine<?, ?, ?> getQueryEngine(@Nullable SortAccessor<?> sortAccessor,
			RepositoryConfigurationSource source) {

		Class<? extends QueryEngineFactory> queryEngineFactoryType = getClassAttribute(source, "queryEngineFactory");

		if (queryEngineFactoryType == null) {
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

	private static <T> @Nullable Class<T> getClassAttribute(RepositoryConfigurationSource source, String attributeName) {
		return source.getAttribute(attributeName, Class.class).filter(Predicate.not(Class::isInterface)).orElse(null);
	}

}
