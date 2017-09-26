/*
 * Copyright 2014-2017 the original author or authors.
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
package org.springframework.data.keyvalue.repository.query;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.SpelCriteria;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link RepositoryQuery} implementation deriving queries from {@link PartTree} using a predefined
 * {@link AbstractQueryCreator}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValuePartTreeQuery implements RepositoryQuery {

	private final EvaluationContextProvider evaluationContextProvider;
	private final QueryMethod queryMethod;
	private final KeyValueOperations keyValueOperations;
	private final QueryCreatorFactory<AbstractQueryCreator<KeyValueQuery<?>, ?>> queryCreatorFactory;

	/**
	 * Creates a new {@link KeyValuePartTreeQuery} for the given {@link QueryMethod}, {@link EvaluationContextProvider},
	 * {@link KeyValueOperations} and query creator type.
	 *
	 * @param queryMethod must not be {@literal null}.
	 * @param evaluationContextProvider must not be {@literal null}.
	 * @param keyValueOperations must not be {@literal null}.
	 * @param queryCreator must not be {@literal null}.
	 */
	public KeyValuePartTreeQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
			KeyValueOperations keyValueOperations, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

		this(queryMethod, evaluationContextProvider, keyValueOperations,
				new ConstructorCachingQueryCreatorFactory(queryCreator));
	}

	/**
	 * Creates a new {@link KeyValuePartTreeQuery} for the given {@link QueryMethod}, {@link EvaluationContextProvider},
	 * {@link KeyValueOperations} using the given {@link QueryCreatorFactory} producing the {@link AbstractQueryCreator}
	 * in charge of altering the query.
	 *
	 * @param queryMethod must not be {@literal null}.
	 * @param evaluationContextProvider must not be {@literal null}.
	 * @param keyValueOperations must not be {@literal null}.
	 * @param queryCreatorFactory must not be {@literal null}.
	 * @since 2.0
	 */
	public KeyValuePartTreeQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
			KeyValueOperations keyValueOperations, QueryCreatorFactory queryCreatorFactory) {

		Assert.notNull(queryMethod, "Query method must not be null!");
		Assert.notNull(evaluationContextProvider, "EvaluationContextprovider must not be null!");
		Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
		Assert.notNull(queryCreatorFactory, "QueryCreatorFactory type must not be null!");

		this.queryMethod = queryMethod;
		this.keyValueOperations = keyValueOperations;
		this.evaluationContextProvider = evaluationContextProvider;
		this.queryCreatorFactory = queryCreatorFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
	 */
	@Override
	public Object execute(Object[] parameters) {

		ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), parameters);
		KeyValueQuery<?> query = prepareQuery(parameters);
		ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

		return processor.processResult(doExecute(parameters, query));
	}

	/**
	 * @param parameters
	 * @param query
	 */
	@Nullable
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object doExecute(Object[] parameters, KeyValueQuery<?> query) {

		if (queryMethod.isPageQuery() || queryMethod.isSliceQuery()) {

			Pageable page = (Pageable) parameters[queryMethod.getParameters().getPageableIndex()];
			query.setOffset(page.getOffset());
			query.setRows(page.getPageSize());

			Iterable<?> result = this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());

			long count = queryMethod.isSliceQuery() ? 0
					: keyValueOperations.count(query, queryMethod.getEntityInformation().getJavaType());

			return new PageImpl(IterableConverter.toList(result), page, count);

		} else if (queryMethod.isCollectionQuery()) {

			return this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());

		} else if (queryMethod.isQueryForEntity()) {

			Iterable<?> result = this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());
			return result.iterator().hasNext() ? result.iterator().next() : null;
		}

		throw new UnsupportedOperationException("Query method not supported.");
	}

	protected KeyValueQuery<?> prepareQuery(Object[] parameters) {

		return prepareQuery(createQuery(new ParametersParameterAccessor(getQueryMethod().getParameters(), parameters)),
				parameters);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected KeyValueQuery<?> prepareQuery(KeyValueQuery<?> instance, Object[] parameters) {

		ParametersParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(),
				parameters);

		Object criteria = instance.getCriteria();

		if (criteria instanceof SpelCriteria || criteria instanceof SpelExpression) {

			SpelExpression spelExpression = getSpelExpression(criteria);
			EvaluationContext context = this.evaluationContextProvider.getEvaluationContext(getQueryMethod().getParameters(),
					parameters);
			criteria = new SpelCriteria(spelExpression, context);
		}

		KeyValueQuery<?> query = new KeyValueQuery(criteria);
		Pageable pageable = accessor.getPageable();
		Sort sort = accessor.getSort();

		query.setOffset(pageable.toOptional().map(Pageable::getOffset).orElse(-1L));

		if (pageable.isPaged()) {
			query.setRows(pageable.getPageSize());
		} else if (instance.getRows() >= 0) {
			query.setRows(instance.getRows());
		}

		query.setSort(sort.isUnsorted() ? instance.getSort() : sort);

		return query;
	}

	private SpelExpression getSpelExpression(Object criteria) {

		if (criteria instanceof SpelExpression) {
			return (SpelExpression) criteria;
		}

		if (criteria instanceof SpelCriteria) {
			return getSpelExpression(((SpelCriteria) criteria).getExpression());
		}

		throw new IllegalStateException(String.format("Cannot retrieve SpelExpression from %s", criteria));
	}

	/**
	 * Create a {@link KeyValueQuery} given {@link ParameterAccessor}.
	 *
	 * @param accessor must not be {@literal null}.
	 * @return the {@link KeyValueQuery}.
	 */
	public KeyValueQuery<?> createQuery(ParameterAccessor accessor) {

		PartTree tree = new PartTree(getQueryMethod().getName(), getQueryMethod().getEntityInformation().getJavaType());

		AbstractQueryCreator<? extends KeyValueQuery<?>, ?> queryCreator = queryCreatorFactory.queryCreatorFor(tree,
				accessor);

		KeyValueQuery<?> query = queryCreator.createQuery();

		if (tree.isLimiting()) {
			query.setRows(tree.getMaxResults());
		}
		return query;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
	 */
	@Override
	public QueryMethod getQueryMethod() {
		return queryMethod;
	}

	/**
	 * Factory class for obtaining {@link AbstractQueryCreator} instances for a given {@link PartTree} and
	 * {@link ParameterAccessor}.
	 *
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	public interface QueryCreatorFactory<T extends AbstractQueryCreator> {

		T queryCreatorFor(PartTree partTree, ParameterAccessor accessor);
	}

	/**
	 * {@link QueryCreatorFactory} implementation instantiating {@link AbstractQueryCreator} via reflection. Looks up and
	 * caches the constructor on creation.
	 *
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	private static class ConstructorCachingQueryCreatorFactory
			implements QueryCreatorFactory<AbstractQueryCreator<KeyValueQuery<?>, ?>> {

		private final Class<?> type;
		private final @Nullable Constructor<? extends AbstractQueryCreator<?, ?>> constructor;

		ConstructorCachingQueryCreatorFactory(Class<? extends AbstractQueryCreator<?, ?>> type) {

			this.type = type;
			this.constructor = ClassUtils.getConstructorIfAvailable(type, PartTree.class, ParameterAccessor.class);
		}

		@Override
		public AbstractQueryCreator<KeyValueQuery<?>, ?> queryCreatorFor(PartTree partTree, ParameterAccessor accessor) {

			Assert.state(constructor != null,
					() -> String.format("No constructor (PartTree, ParameterAccessor) could be found on type %s!", type));
			return (AbstractQueryCreator<KeyValueQuery<?>, ?>) BeanUtils.instantiateClass(constructor, partTree, accessor);
		}
	}
}
