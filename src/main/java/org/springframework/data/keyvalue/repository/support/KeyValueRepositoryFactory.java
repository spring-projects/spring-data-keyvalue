/*
 * Copyright 2014-2019 the original author or authors.
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
package org.springframework.data.keyvalue.repository.support;

import static org.springframework.data.querydsl.QuerydslUtils.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link RepositoryFactorySupport} specific of handing
 * {@link org.springframework.data.keyvalue.repository.KeyValueRepository}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueRepositoryFactory extends RepositoryFactorySupport {

	private static final Class<SpelQueryCreator> DEFAULT_QUERY_CREATOR = SpelQueryCreator.class;

	private final KeyValueOperations keyValueOperations;
	private final MappingContext<?, ?> context;
	private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
	private final Class<? extends RepositoryQuery> repositoryQueryType;

	/**
	 * Creates a new {@link KeyValueRepositoryFactory} for the given {@link KeyValueOperations}.
	 *
	 * @param keyValueOperations must not be {@literal null}.
	 */
	public KeyValueRepositoryFactory(KeyValueOperations keyValueOperations) {
		this(keyValueOperations, DEFAULT_QUERY_CREATOR);
	}

	/**
	 * Creates a new {@link KeyValueRepositoryFactory} for the given {@link KeyValueOperations} and
	 * {@link AbstractQueryCreator}-type.
	 *
	 * @param keyValueOperations must not be {@literal null}.
	 * @param queryCreator must not be {@literal null}.
	 */
	public KeyValueRepositoryFactory(KeyValueOperations keyValueOperations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

		this(keyValueOperations, queryCreator, KeyValuePartTreeQuery.class);
	}

	/**
	 * Creates a new {@link KeyValueRepositoryFactory} for the given {@link KeyValueOperations} and
	 * {@link AbstractQueryCreator}-type.
	 *
	 * @param keyValueOperations must not be {@literal null}.
	 * @param queryCreator must not be {@literal null}.
	 * @param repositoryQueryType must not be {@literal null}.
	 * @since 1.1
	 */
	public KeyValueRepositoryFactory(KeyValueOperations keyValueOperations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {

		Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
		Assert.notNull(queryCreator, "Query creator type must not be null!");
		Assert.notNull(repositoryQueryType, "RepositoryQueryType type must not be null!");

		this.queryCreator = queryCreator;
		this.keyValueOperations = keyValueOperations;
		this.context = keyValueOperations.getMappingContext();
		this.repositoryQueryType = repositoryQueryType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getEntityInformation(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {

		PersistentEntity<T, ?> entity = (PersistentEntity<T, ?>) context.getRequiredPersistentEntity(domainClass);

		return new PersistentEntityInformation<>(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getTargetRepository(org.springframework.data.repository.core.RepositoryMetadata)
	 */
	@Override
	protected Object getTargetRepository(RepositoryInformation repositoryInformation) {

		EntityInformation<?, ?> entityInformation = getEntityInformation(repositoryInformation.getDomainType());
		return super.getTargetRepositoryViaReflection(repositoryInformation, entityInformation, keyValueOperations);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
	 */
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return isQueryDslRepository(metadata.getRepositoryInterface()) ? QuerydslKeyValueRepository.class
				: SimpleKeyValueRepository.class;
	}

	/**
	 * Returns whether the given repository interface requires a QueryDsl specific implementation to be chosen.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 * @return
	 */
	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getQueryLookupStrategy(org.springframework.data.repository.query.QueryLookupStrategy.Key, org.springframework.data.repository.query.EvaluationContextProvider)
	 */
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.of(new KeyValueQueryLookupStrategy(key, evaluationContextProvider, this.keyValueOperations,
				this.queryCreator, this.repositoryQueryType));
	}

	/**
	 * @author Christoph Strobl
	 * @author Oliver Gierke
	 */
	private static class KeyValueQueryLookupStrategy implements QueryLookupStrategy {

		private QueryMethodEvaluationContextProvider evaluationContextProvider;
		private KeyValueOperations keyValueOperations;

		private Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
		private Class<? extends RepositoryQuery> repositoryQueryType;

		/**
		 * @param key
		 * @param evaluationContextProvider
		 * @param keyValueOperations
		 * @param queryCreator
		 * @since 1.1
		 */
		public KeyValueQueryLookupStrategy(@Nullable Key key,
				QueryMethodEvaluationContextProvider evaluationContextProvider, KeyValueOperations keyValueOperations,
				Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
				Class<? extends RepositoryQuery> repositoryQueryType) {

			Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");
			Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
			Assert.notNull(queryCreator, "Query creator type must not be null!");
			Assert.notNull(repositoryQueryType, "RepositoryQueryType type must not be null!");

			this.evaluationContextProvider = evaluationContextProvider;
			this.keyValueOperations = keyValueOperations;
			this.queryCreator = queryCreator;
			this.repositoryQueryType = repositoryQueryType;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
				NamedQueries namedQueries) {

			QueryMethod queryMethod = new QueryMethod(method, metadata, factory);

			Constructor<? extends KeyValuePartTreeQuery> constructor = (Constructor<? extends KeyValuePartTreeQuery>) ClassUtils
					.getConstructorIfAvailable(this.repositoryQueryType, QueryMethod.class,
							QueryMethodEvaluationContextProvider.class, KeyValueOperations.class, Class.class);

			Assert.state(constructor != null,
					String.format(
							"Constructor %s(QueryMethod, EvaluationContextProvider, KeyValueOperations, Class) not available!",
							ClassUtils.getShortName(this.repositoryQueryType)));

			return BeanUtils.instantiateClass(constructor, queryMethod, evaluationContextProvider, this.keyValueOperations,
					this.queryCreator);
		}
	}
}
