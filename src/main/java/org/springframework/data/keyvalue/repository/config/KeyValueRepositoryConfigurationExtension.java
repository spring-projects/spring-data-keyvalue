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
package org.springframework.data.keyvalue.repository.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.util.ObjectUtils;

/**
 * {@link RepositoryConfigurationExtension} for {@link KeyValueRepository}.
 * 
 * @author Christoph Strobl
 * @since 1.10
 */
public class KeyValueRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

	public static final String MAPPING_CONTEXT_BEAN_NAME = "keyValueMappingContext";
	public static final String KEY_VALUE_TEMPLATE_BEAN_NAME = "keyValueTemplate";
	public static final String Key_VALUE_TEMPLATE_BEAN_NAME_CONFIG_ATTRIBUTE = KEY_VALUE_TEMPLATE_BEAN_NAME + "Ref";

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtension#getRepositoryFactoryClassName()
	 */
	@Override
	public String getRepositoryFactoryClassName() {
		return KeyValueRepositoryFactoryBean.class.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "KeyValue";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModulePrefix()
	 */
	@Override
	protected String getModulePrefix() {
		return "keyvalue";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
	 */
	@Override
	protected Collection<Class<?>> getIdentifyingTypes() {
		return Collections.<Class<?>> singleton(KeyValueRepository.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
	 */
	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

		AnnotationAttributes attributes = config.getAttributes();

		builder.addPropertyReference("keyValueOperations",
				attributes.getString(Key_VALUE_TEMPLATE_BEAN_NAME_CONFIG_ATTRIBUTE));
		builder.addPropertyValue("queryCreator", getQueryCreatorType(config));
		builder.addPropertyReference("mappingContext", MAPPING_CONTEXT_BEAN_NAME);
	}

	/**
	 * Detects the query creator type to be used for the factory to set. Will lookup a {@link QueryCreatorType} annotation
	 * on the {@code @Enable}-annotation or use {@link SpelQueryCreator} if not found.
	 * 
	 * @param config
	 * @return
	 */
	private static Class<?> getQueryCreatorType(AnnotationRepositoryConfigurationSource config) {

		AnnotationMetadata metadata = config.getEnableAnnotationMetadata();

		Map<String, Object> queryCreatorFoo = metadata.getAnnotationAttributes(QueryCreatorType.class.getName());

		if (queryCreatorFoo == null) {
			return SpelQueryCreator.class;
		}

		AnnotationAttributes queryCreatorAttributes = new AnnotationAttributes(queryCreatorFoo);
		return queryCreatorAttributes.getClass("value");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#registerBeansForRoot(org.springframework.beans.factory.support.BeanDefinitionRegistry, org.springframework.data.repository.config.RepositoryConfigurationSource)
	 */
	@Override
	public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource configurationSource) {

		super.registerBeansForRoot(registry, configurationSource);

		potentiallyRegisterMappingContext(registry, configurationSource);
		potentiallyRegisterKeyValueTemplate(registry, configurationSource);
	}

	private void potentiallyRegisterKeyValueTemplate(BeanDefinitionRegistry registry,
			RepositoryConfigurationSource configurationSource) {

		if (ObjectUtils.nullSafeEquals(configurationSource.getAttribute(Key_VALUE_TEMPLATE_BEAN_NAME_CONFIG_ATTRIBUTE),
				KEY_VALUE_TEMPLATE_BEAN_NAME)) {

			RootBeanDefinition defaultKeyValueTemplateBeanDefinition = getDefaultKeyValueTemplateBeanDefinition();
			if (defaultKeyValueTemplateBeanDefinition != null) {
				registerIfNotAlreadyRegistered(defaultKeyValueTemplateBeanDefinition, registry, KEY_VALUE_TEMPLATE_BEAN_NAME,
						configurationSource);
			}
		}
	}

	private void potentiallyRegisterMappingContext(BeanDefinitionRegistry registry,
			RepositoryConfigurationSource configurationSource) {

		RootBeanDefinition mappingContextDefinition = new RootBeanDefinition(KeyValueMappingContext.class);
		mappingContextDefinition.setSource(configurationSource.getSource());
		registry.registerBeanDefinition(MAPPING_CONTEXT_BEAN_NAME, mappingContextDefinition);

		registerIfNotAlreadyRegistered(mappingContextDefinition, registry, MAPPING_CONTEXT_BEAN_NAME, configurationSource);
	}

	/**
	 * Get the default {@link RootBeanDefinition} for {@link org.springframework.data.keyvalue.core.KeyValueTemplate}.
	 * 
	 * @return {@literal null} to explicitly not register a template.
	 */
	protected RootBeanDefinition getDefaultKeyValueTemplateBeanDefinition() {
		return null;
	}

}
