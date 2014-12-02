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

import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.map.MapKeyValueAdapterFactory;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

/**
 * @author Christoph Strobl
 */
public class MapRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "Map";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModulePrefix()
	 */
	@Override
	protected String getModulePrefix() {
		return "map";
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getDefaultKeyValueTemplateRef()
	 */
	@Override
	protected String getDefaultKeyValueTemplateRef() {
		return "mapKeyValueTemplate";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getDefaultKeyValueTemplateBeanDefinition()
	 */
	@Override
	protected RootBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
			RepositoryConfigurationSource configurationSource) {

		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();

		GenericBeanDefinition referencingMapKeyValueAdapterBeanDefintion = new GenericBeanDefinition();
		referencingMapKeyValueAdapterBeanDefintion.setBeanClass(MapKeyValueAdapter.class);
		referencingMapKeyValueAdapterBeanDefintion.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

		constructorArgumentValues.addGenericArgumentValue(referencingMapKeyValueAdapterBeanDefintion);

		RootBeanDefinition keyValueTemplateDefinition = new RootBeanDefinition(KeyValueTemplate.class);
		keyValueTemplateDefinition.setConstructorArgumentValues(constructorArgumentValues);
		keyValueTemplateDefinition.setRole(BeanDefinition.ROLE_APPLICATION);

		return keyValueTemplateDefinition;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void registerTemplateInfrastructure(BeanDefinitionRegistry registry,
			RepositoryConfigurationSource configurationSource) {

		Class<? extends Map> type = (Class<? extends Map>) ((AnnotationMetadata) configurationSource.getSource())
				.getAnnotationAttributes(EnableMapRepositories.class.getName()).get("mapType");

		ConstructorArgumentValues mapAdapterFactoryArgs = new ConstructorArgumentValues();
		mapAdapterFactoryArgs.addGenericArgumentValue(type);
		RootBeanDefinition mapAdapterFactory = new RootBeanDefinition(MapKeyValueAdapterFactory.class,
				mapAdapterFactoryArgs, null);

		registry.registerBeanDefinition("mapKeyValueAdapterFactory", mapAdapterFactory);

		RootBeanDefinition mapKeyValueAdapter = new RootBeanDefinition(MapKeyValueAdapter.class);
		mapKeyValueAdapter.setFactoryBeanName("mapKeyValueAdapterFactory");
		mapKeyValueAdapter.setFactoryMethodName("getAdapter");

		registry.registerBeanDefinition("mapKeyValueAdapter", mapAdapterFactory);
	}
}
