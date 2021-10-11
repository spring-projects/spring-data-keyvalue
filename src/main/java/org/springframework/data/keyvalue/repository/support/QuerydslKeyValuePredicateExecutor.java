/*
 * Copyright 2021 the original author or authors.
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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.querydsl.collections.AbstractCollQuery;
import com.querydsl.collections.CollQuery;
import com.querydsl.core.NonUniqueResultException;
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
public class QuerydslKeyValuePredicateExecutor<T> implements QuerydslPredicateExecutor<T> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

	private final PathBuilder<T> builder;
	private final Supplier<Iterable<T>> findAll;

	/**
	 * Creates a new {@link QuerydslKeyValuePredicateExecutor} for the given {@link EntityInformation}.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 */
	public QuerydslKeyValuePredicateExecutor(EntityInformation<T, ?> entityInformation, KeyValueOperations operations) {
		this(entityInformation, operations, DEFAULT_ENTITY_PATH_RESOLVER);
	}

	/**
	 * Creates a new {@link QuerydslKeyValuePredicateExecutor} for the given {@link EntityInformation}, and
	 * {@link EntityPathResolver}.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 * @param resolver must not be {@literal null}.
	 */
	public QuerydslKeyValuePredicateExecutor(EntityInformation<T, ?> entityInformation, KeyValueOperations operations,
			EntityPathResolver resolver) {

		Assert.notNull(resolver, "EntityPathResolver must not be null!");

		EntityPath<T> path = resolver.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
		findAll = () -> operations.findAll(entityInformation.getJavaType());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findOne(com.querydsl.core.types.Predicate)
	 */
	@Override
	public Optional<T> findOne(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		try {
			return Optional.ofNullable(prepareQuery(predicate).fetchOne());
		} catch (NonUniqueResultException o_O) {
			throw new IncorrectResultSizeDataAccessException("Expected one or no result but found more than one!", 1, o_O);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.querydsl.core.types.Predicate)
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return prepareQuery(predicate).fetchResults().getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, com.querydsl.core.types.OrderSpecifier[])
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(orders, "OrderSpecifiers must not be null!");

		AbstractCollQuery<T, ?> query = prepareQuery(predicate);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, org.springframework.data.domain.Sort)
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate, Sort sort) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(sort, "Sort must not be null!");

		return findAll(predicate, toOrderSpecifier(sort, builder));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(pageable, "Pageable must not be null!");

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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.querydsl.core.types.OrderSpecifier[])
	 */
	@Override
	public Iterable<T> findAll(OrderSpecifier<?>... orders) {

		Assert.notNull(orders, "OrderSpecifiers must not be null!");

		if (orders.length == 0) {
			return findAll.get();
		}

		AbstractCollQuery<T, ?> query = prepareQuery(null);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#count(com.querydsl.core.types.Predicate)
	 */
	@Override
	public long count(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return prepareQuery(predicate).fetchCount();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#exists(com.querydsl.core.types.Predicate)
	 */
	@Override
	public boolean exists(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return count(predicate) > 0;
	}

	@Override
	public <S extends T, R> R findBy(Predicate predicate,
			Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
		throw new UnsupportedOperationException("Not yet supported");
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
}