/*
 * Copyright 2021-2022 the original author or authors.
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

import static org.springframework.data.keyvalue.repository.support.KeyValueQuerydslUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.convert.DtoInstantiatingConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.ListQuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.querydsl.collections.AbstractCollQuery;
import com.querydsl.collections.CollQuery;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * {@link QuerydslPredicateExecutor} capable of applying {@link Predicate}s using {@link CollQuery}.
 *
 * @author Mark Paluch
 * @since 2.6
 */
public class QuerydslKeyValuePredicateExecutor<T> implements ListQuerydslPredicateExecutor<T> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

	private final MappingContext<? extends PersistentEntity<?, ?>, ? extends PersistentProperty<?>> context;
	private final PathBuilder<T> builder;
	private final Supplier<List<T>> findAll;
	private final EntityInformation<T, ?> entityInformation;
	private final ProjectionFactory projectionFactory;
	private final EntityInstantiators entityInstantiators = new EntityInstantiators();

	/**
	 * Creates a new {@link QuerydslKeyValuePredicateExecutor} for the given {@link EntityInformation}.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 */
	public QuerydslKeyValuePredicateExecutor(EntityInformation<T, ?> entityInformation, KeyValueOperations operations) {
		this(entityInformation, new SpelAwareProxyProjectionFactory(), operations, DEFAULT_ENTITY_PATH_RESOLVER);
	}

	/**
	 * Creates a new {@link QuerydslKeyValuePredicateExecutor} for the given {@link EntityInformation}, and
	 * {@link EntityPathResolver}.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param projectionFactory must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 * @param resolver must not be {@literal null}.
	 */
	public QuerydslKeyValuePredicateExecutor(EntityInformation<T, ?> entityInformation,
			ProjectionFactory projectionFactory, KeyValueOperations operations,
			EntityPathResolver resolver) {

		Assert.notNull(entityInformation, "EntityInformation must not be null");
		Assert.notNull(projectionFactory, "ProjectionFactory must not be null");
		Assert.notNull(operations, "KeyValueOperations must not be null");
		Assert.notNull(resolver, "EntityPathResolver must not be null");

		this.projectionFactory = projectionFactory;
		this.context = operations.getMappingContext();
		EntityPath<T> path = resolver.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
		this.entityInformation = entityInformation;
		findAll = () -> IterableConverter.toList(operations.findAll(entityInformation.getJavaType()));
	}

	@Override
	public Optional<T> findOne(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null");

		try {
			return Optional.ofNullable(prepareQuery(predicate).fetchOne());
		} catch (NonUniqueResultException o_O) {
			throw new IncorrectResultSizeDataAccessException("Expected one or no result but found more than one", 1, o_O);
		}
	}

	@Override
	public List<T> findAll(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null");

		return prepareQuery(predicate).fetchResults().getResults();
	}

	@Override
	public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {

		Assert.notNull(predicate, "Predicate must not be null");
		Assert.notNull(orders, "OrderSpecifiers must not be null");

		AbstractCollQuery<T, ?> query = prepareQuery(predicate);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	@Override
	public List<T> findAll(Predicate predicate, Sort sort) {

		Assert.notNull(predicate, "Predicate must not be null");
		Assert.notNull(sort, "Sort must not be null");

		return findAll(predicate, toOrderSpecifier(sort, builder));
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

		Assert.notNull(predicate, "Predicate must not be null");
		Assert.notNull(pageable, "Pageable must not be null");

		AbstractCollQuery<T, ?> query = prepareQuery(predicate);

		if (pageable.isPaged() || pageable.getSort().isSorted()) {

			query.offset(pageable.getOffset());
			query.limit(pageable.getPageSize());

			if (pageable.getSort().isSorted()) {
				query.orderBy(toOrderSpecifier(pageable.getSort(), builder));
			}
		}

		return new PageImpl<>(query.fetchResults().getResults(), pageable, count(predicate));
	}

	@Override
	public List<T> findAll(OrderSpecifier<?>... orders) {

		Assert.notNull(orders, "OrderSpecifiers must not be null");

		if (orders.length == 0) {
			return findAll.get();
		}

		AbstractCollQuery<T, ?> query = prepareQuery(null);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	@Override
	public long count(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null");

		return prepareQuery(predicate).fetchCount();
	}

	@Override
	public boolean exists(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null");

		return count(predicate) > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends T, R> R findBy(Predicate predicate,
			Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {

		Assert.notNull(predicate, "Predicate must not be null");
		Assert.notNull(queryFunction, "Query function must not be null");

		return queryFunction.apply(new FluentQuerydsl<>(predicate, (Class<S>) entityInformation.getJavaType()));
	}

	/**
	 * Creates executable query for given {@link Predicate}.
	 *
	 * @param predicate
	 * @return
	 */
	protected AbstractCollQuery<T, ?> prepareQuery(@Nullable Predicate predicate) {

		CollQuery<T> query = new CollQuery<>();
		query.from(builder, findAll.get());

		return predicate != null ? query.where(predicate) : query;
	}

	/**
	 * {@link org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery} using Querydsl
	 * {@link Predicate}.
	 *
	 * @author Mark Paluch
	 * @since 2.6
	 */
	class FluentQuerydsl<R> implements FluentQuery.FetchableFluentQuery<R> {

		private final Predicate predicate;
		private final Sort sort;
		private final Class<?> entityType;
		private final Class<R> resultType;
		private final List<String> fieldsToInclude;

		FluentQuerydsl(Predicate predicate, Class<R> resultType) {
			this(predicate, Sort.unsorted(), resultType, resultType, Collections.emptyList());
		}

		public FluentQuerydsl(Predicate predicate, Sort sort, Class<?> entityType, Class<R> resultType,
				List<String> fieldsToInclude) {
			this.predicate = predicate;
			this.sort = sort;
			this.entityType = entityType;
			this.resultType = resultType;
			this.fieldsToInclude = fieldsToInclude;
		}

		@Override
		public FluentQuery.FetchableFluentQuery<R> sortBy(Sort sort) {

			Assert.notNull(sort, "Sort must not be null");

			return new FluentQuerydsl<>(predicate, sort, entityType, resultType, fieldsToInclude);
		}

		@Override
		public <NR> FluentQuery.FetchableFluentQuery<NR> as(Class<NR> projection) {

			Assert.notNull(projection, "Projection target type must not be null");

			return new FluentQuerydsl<>(predicate, sort, entityType, projection, fieldsToInclude);
		}

		public FluentQuery.FetchableFluentQuery<R> project(Collection<String> properties) {

			Assert.notNull(properties, "Projection properties must not be null");

			return new FluentQuerydsl<>(predicate, sort, entityType, resultType, new ArrayList<>(properties));
		}

		@Override
		public R oneValue() {

			List<T> results = createQuery().limit(2).fetch();

			if (results.isEmpty()) {
				return null;
			}

			if (results.size() > 1) {
				throw new IncorrectResultSizeDataAccessException(1);
			}

			T one = results.get(0);
			return getConversionFunction().apply(one);

		}

		@Override
		public R firstValue() {

			List<T> results = createQuery().limit(1).fetch();

			if (results.isEmpty()) {
				return null;
			}

			T one = results.get(0);
			return getConversionFunction().apply(one);
		}

		@Override
		public List<R> all() {

			List<T> results = createQuery().fetch();

			return mapResults(results);
		}

		@Override
		public Page<R> page(Pageable pageable) {

			Assert.notNull(pageable, "Pageable must not be null");

			AbstractCollQuery<T, ?> query = createQuery();

			if (pageable.isPaged() || pageable.getSort().isSorted()) {

				query.offset(pageable.getOffset());
				query.limit(pageable.getPageSize());

				if (pageable.getSort().isSorted()) {
					query.orderBy(toOrderSpecifier(pageable.getSort(), builder));
				}
			}
			QueryResults<T> results = query.limit(pageable.getPageSize()).offset(pageable.getOffset()).fetchResults();

			return new PageImpl<>(mapResults(results.getResults()), pageable, results.getTotal());
		}

		@Override
		public Stream<R> stream() {
			return createQuery().stream().map(getConversionFunction());
		}

		@Override
		public long count() {
			return createQuery().fetchCount();
		}

		@Override
		public boolean exists() {
			return count() > 0;
		}

		private AbstractCollQuery<T, ?> createQuery() {

			AbstractCollQuery<T, ?> query = prepareQuery(predicate);

			if (sort.isSorted()) {
				query.orderBy(toOrderSpecifier(sort, builder));
			}
			return query;
		}

		@SuppressWarnings("unchecked")
		private List<R> mapResults(List<T> results) {

			if (entityType == resultType) {
				return (List<R>) results;
			}

			List<R> mapped = new ArrayList<>(results.size());

			Function<Object, R> converter = getConversionFunction();
			for (T result : results) {
				mapped.add(converter.apply(result));
			}

			return mapped;
		}

		@SuppressWarnings("unchecked")
		private <P> Function<Object, P> getConversionFunction(Class<?> inputType, Class<P> targetType) {

			if (targetType.isAssignableFrom(inputType)) {
				return (Function<Object, P>) Function.identity();
			}

			if (targetType.isInterface()) {
				return o -> projectionFactory.createProjection(targetType, o);
			}

			DtoInstantiatingConverter converter = new DtoInstantiatingConverter(targetType, context, entityInstantiators);

			return o -> (P) converter.convert(o);
		}

		private Function<Object, R> getConversionFunction() {
			return getConversionFunction(entityType, resultType);
		}

	}
}
