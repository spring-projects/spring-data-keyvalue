/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.data.keyvalue.repository.support;

import java.io.Serializable;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.FactoryBean} to create {@link KeyValueRepository}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class KeyValueRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends RepositoryFactoryBeanSupport<T, S, ID> {

	private KeyValueOperations operations;
	private Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
	private Class<? extends RepositoryQuery> repositoryQueryType;

	/**
	 * Creates a new {@link KeyValueRepositoryFactoryBean} for the given repository interface.
	 * 
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public KeyValueRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/**
	 * Configures the {@link KeyValueOperations} to be used for the repositories.
	 * 
	 * @param operations must not be {@literal null}.
	 */
	public void setKeyValueOperations(KeyValueOperations operations) {

		Assert.notNull(operations, "KeyValueOperations must not be null!");

		this.operations = operations;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#setMappingContext(org.springframework.data.mapping.context.MappingContext)
	 */
	@Override
	public void setMappingContext(MappingContext<?, ?> mappingContext) {
		super.setMappingContext(mappingContext);
	}

	/**
	 * Configures the {@link QueryCreatorType} to be used.
	 * 
	 * @param queryCreator must not be {@literal null}.
	 */
	public void setQueryCreator(Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

		Assert.notNull(queryCreator, "Query creator type must not be null!");

		this.queryCreator = queryCreator;
	}

	/**
	 * Configures the {@link RepositoryQuery} type to be created.
	 *
	 * @param repositoryQueryType must not be {@literal null}.
	 * @since 1.1
	 */
	public void setQueryType(Class<? extends RepositoryQuery> repositoryQueryType) {

		Assert.notNull(queryCreator, "Query creator type must not be null!");

		this.repositoryQueryType = repositoryQueryType;

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#createRepositoryFactory()
	 */
	@Override
	protected final RepositoryFactorySupport createRepositoryFactory() {
		return createRepositoryFactory(operations, queryCreator, repositoryQueryType);
	}

	/**
	 * Create the repository factory to be used to create repositories.
	 *
	 * @param operations will never be {@literal null}.
	 * @param queryCreator will never be {@literal null}.
	 * @param repositoryQueryType will never be {@literal null}.
	 * @return must not be {@literal null}.
	 * @since 1.1
	 */
	protected KeyValueRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {

		return new KeyValueRepositoryFactory(operations, queryCreator, repositoryQueryType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.notNull(operations, "KeyValueOperations must not be null!");
		Assert.notNull(queryCreator, "Query creator type must not be null!");
		Assert.notNull(repositoryQueryType, "RepositoryQueryType type type must not be null!");

		super.afterPropertiesSet();
	}
}
