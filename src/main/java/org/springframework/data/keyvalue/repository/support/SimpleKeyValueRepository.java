/*
 * Copyright 2014-2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;

/**
 * Simple {@link KeyValueRepository} implementation.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Eugene Nikiforov
 * @param <T>
 * @param <ID>
 */
public class SimpleKeyValueRepository<T, ID> implements KeyValueRepository<T, ID> {

	private final KeyValueOperations operations;
	private final EntityInformation<T, ID> entityInformation;

	/**
	 * Creates a new {@link SimpleKeyValueRepository} for the given {@link EntityInformation} and
	 * {@link KeyValueOperations}.
	 *
	 * @param metadata must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 */
	public SimpleKeyValueRepository(EntityInformation<T, ID> metadata, KeyValueOperations operations) {

		Assert.notNull(metadata, "EntityInformation must not be null");
		Assert.notNull(operations, "KeyValueOperations must not be null");

		this.entityInformation = metadata;
		this.operations = operations;
	}

	// -------------------------------------------------------------------------
	// Methods from CrudRepository
	// -------------------------------------------------------------------------

	@Override
	public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Entity must not be null");

		if (entityInformation.isNew(entity)) {
			return operations.insert(entity);
		}

		return operations.update(entityInformation.getRequiredId(entity), entity);
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null");

		List<S> saved = new ArrayList<>();

		for (S entity : entities) {
			saved.add(save(entity));
		}

		return saved;
	}

	@Override
	public Optional<T> findById(ID id) {

		Assert.notNull(id, "The given id must not be null");

		return operations.findById(id, entityInformation.getJavaType());
	}

	@Override
	public boolean existsById(ID id) {
		return findById(id).isPresent();
	}

	@Override
	public List<T> findAll() {
		return IterableConverter.toList(operations.findAll(entityInformation.getJavaType()));
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {

		Assert.notNull(ids, "The given Iterable of id's must not be null");

		List<T> result = new ArrayList<>();

		ids.forEach(id -> findById(id).ifPresent(result::add));

		return result;
	}

	@Override
	public long count() {
		return operations.count(entityInformation.getJavaType());
	}

	@Override
	public void deleteById(ID id) {

		Assert.notNull(id, "The given id must not be null");

		operations.delete(id, entityInformation.getJavaType());
	}

	@Override
	public void delete(T entity) {

		Assert.notNull(entity, "The given entity must not be null");

		operations.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {

		Assert.notNull(ids, "The given Iterable of Ids must not be null");

		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null");

		entities.forEach(this::delete);
	}

	@Override
	public void deleteAll() {
		operations.delete(entityInformation.getJavaType());
	}

	// -------------------------------------------------------------------------
	// Methods from PagingAndSortingRepository
	// -------------------------------------------------------------------------

	@Override
	public List<T> findAll(Sort sort) {

		Assert.notNull(sort, "Sort must not be null");

		return IterableConverter.toList(operations.findAll(sort, entityInformation.getJavaType()));
	}

	@Override
	public Page<T> findAll(Pageable pageable) {

		Assert.notNull(pageable, "Pageable must not be null");

		if (pageable.isUnpaged()) {
			List<T> result = findAll();
			return new PageImpl<>(result, Pageable.unpaged(), result.size());
		}

		Iterable<T> content = operations.findInRange(pageable.getOffset(), pageable.getPageSize(), pageable.getSort(),
				entityInformation.getJavaType());

		return new PageImpl<>(IterableConverter.toList(content), pageable,
				this.operations.count(entityInformation.getJavaType()));
	}
}
