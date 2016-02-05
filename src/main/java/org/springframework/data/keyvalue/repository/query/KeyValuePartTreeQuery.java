/*
 * Copyright 2014-2015 the original author or authors.
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
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link RepositoryQuery} implementation deriving queries from {@link PartTree} using a predefined
 * {@link AbstractQueryCreator}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class KeyValuePartTreeQuery implements RepositoryQuery {

	private final EvaluationContextProvider evaluationContextProvider;
	private final QueryMethod queryMethod;
	private final KeyValueOperations keyValueOperations;
	private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;

	private KeyValueQuery<?> query;

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

		Assert.notNull(queryMethod, "Query method must not be null!");
		Assert.notNull(evaluationContextProvider, "EvaluationContextprovider must not be null!");
		Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
		Assert.notNull(queryCreator, "QueryCreator type must not be null!");

		this.queryMethod = queryMethod;
		this.keyValueOperations = keyValueOperations;
		this.evaluationContextProvider = evaluationContextProvider;
		this.queryCreator = queryCreator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
	 */
	@Override
	public QueryMethod getQueryMethod() {
		return queryMethod;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
	 */
	@Override
	public Object execute(Object[] parameters) {

		ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), parameters);
		KeyValueQuery<?> query = prepareQuery(parameters, accessor);
		ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

		return processor.processResult(doExecute(parameters, query));
	}

	/**
	 * @param parameters
	 * @param query
	 */
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private KeyValueQuery<?> prepareQuery(Object[] parameters, ParameterAccessor accessor) {

		if (this.query == null) {
			this.query = createQuery(accessor);
		}

		KeyValueQuery<?> q = new KeyValueQuery(this.query.getCriteria());

		if (accessor.getPageable() != null) {
			q.setOffset(accessor.getPageable().getOffset());
			q.setRows(accessor.getPageable().getPageSize());
		} else {
			q.setOffset(-1);
			q.setRows(-1);
		}

		Sort sort = accessor.getSort();

		q.setSort(sort != null ? sort : query.getSort());

		if (q.getCriteria() instanceof SpelExpression) {
			EvaluationContext context = this.evaluationContextProvider.getEvaluationContext(getQueryMethod().getParameters(),
					parameters);
			((SpelExpression) q.getCriteria()).setEvaluationContext(context);
		}

		return q;
	}

	public KeyValueQuery<?> createQuery(ParameterAccessor accessor) {

		PartTree tree = new PartTree(getQueryMethod().getName(), getQueryMethod().getEntityInformation().getJavaType());

		Constructor<? extends AbstractQueryCreator<?, ?>> constructor = (Constructor<? extends AbstractQueryCreator<?, ?>>) ClassUtils
				.getConstructorIfAvailable(queryCreator, PartTree.class, ParameterAccessor.class);
		return (KeyValueQuery<?>) BeanUtils.instantiateClass(constructor, tree, accessor).createQuery();
	}
}
