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
package org.springframework.data.keyvalue.repository.support;

import static org.springframework.data.keyvalue.repository.support.KeyValueQuerydslUtils.*;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.querydsl.collections.AbstractCollQuery;
import com.querydsl.collections.CollQuery;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * {@link KeyValueRepository} implementation capable of executing {@link Predicate}s using {@link CollQuery}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @param <T> the domain type to manage
 * @param <ID> the identifier type of the domain type
 */
public class QuerydslKeyValueRepository<T, ID extends Serializable> extends SimpleKeyValueRepository<T, ID>
		implements QueryDslPredicateExecutor<T> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

	private final EntityPath<T> path;
	private final PathBuilder<T> builder;

	/**
	 * Creates a new {@link QuerydslKeyValueRepository} for the given {@link EntityInformation} and
	 * {@link KeyValueOperations}.
	 * 
	 * @param entityInformation must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 */
	public QuerydslKeyValueRepository(EntityInformation<T, ID> entityInformation, KeyValueOperations operations) {
		this(entityInformation, operations, DEFAULT_ENTITY_PATH_RESOLVER);
	}

	/**
	 * Creates a new {@link QuerydslKeyValueRepository} for the given {@link EntityInformation},
	 * {@link KeyValueOperations} and {@link EntityPathResolver}.
	 * 
	 * @param entityInformation must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 * @param resolver must not be {@literal null}.
	 */
	public QuerydslKeyValueRepository(EntityInformation<T, ID> entityInformation, KeyValueOperations operations,
			EntityPathResolver resolver) {

		super(entityInformation, operations);

		Assert.notNull(resolver, "EntityPathResolver must not be null!");

		this.path = resolver.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findOne(com.mysema.query.types.Predicate)
	 */
	@Override
	public T findOne(Predicate predicate) {
		return prepareQuery(predicate).fetchOne();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate)
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate) {
		return prepareQuery(predicate).fetchResults().getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, com.mysema.query.types.OrderSpecifier[])
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {

		AbstractCollQuery<T, ?> query = prepareQuery(predicate);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Sort)
	 */
	@Override
	public Iterable<T> findAll(Predicate predicate, Sort sort) {
		return findAll(predicate, toOrderSpecifier(sort, builder));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

		AbstractCollQuery<T, ?> query = prepareQuery(predicate);

		if (pageable != null) {

			query.offset(pageable.getOffset());
			query.limit(pageable.getPageSize());

			if (pageable.getSort() != null) {
				query.orderBy(toOrderSpecifier(pageable.getSort(), builder));
			}
		}

		return new PageImpl<T>(query.fetchResults().getResults(), pageable, count(predicate));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.OrderSpecifier[])
	 */
	@Override
	public Iterable<T> findAll(OrderSpecifier<?>... orders) {

		if (ObjectUtils.isEmpty(orders)) {
			return findAll();
		}

		AbstractCollQuery<T, ?> query = prepareQuery(null);
		query.orderBy(orders);

		return query.fetchResults().getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#count(com.mysema.query.types.Predicate)
	 */
	@Override
	public long count(Predicate predicate) {
		return prepareQuery(predicate).fetchCount();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#exists(com.mysema.query.types.Predicate)
	 */
	@Override
	public boolean exists(Predicate predicate) {
		return count(predicate) > 0;
	}

	/**
	 * Creates executable query for given {@link Predicate}.
	 * 
	 * @param predicate
	 * @return
	 */
	protected AbstractCollQuery<T, ?> prepareQuery(Predicate predicate) {

		CollQuery<T> query = new CollQuery<T>();
		query.from(builder, findAll());

		return predicate != null ? query.where(predicate) : query;
	}
}
