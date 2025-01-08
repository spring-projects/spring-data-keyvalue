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
package org.springframework.data.keyvalue.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

/**
 * @author Christoph Strobl
 * @author Eugene Nikiforov
 * @author Jens Schauder
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class SimpleKeyValueRepositoryUnitTests {

	private SimpleKeyValueRepository<Foo, String> repo;
	private @Mock KeyValueOperations opsMock;
	private KeyValueMappingContext<?, ?> context;

	@BeforeEach
	void setUp() {

		this.context = new KeyValueMappingContext<>();

		EntityInformation<Foo, String> ei = getEntityInformationFor(Foo.class);
		repo = new SimpleKeyValueRepository<>(ei, opsMock);
	}

	@Test // DATACMNS-525
	void saveNewWithNumericId() {

		EntityInformation<WithNumericId, ?> ei = getEntityInformationFor(WithNumericId.class);
		SimpleKeyValueRepository<WithNumericId, ?> temp = new SimpleKeyValueRepository<>(ei, opsMock);

		WithNumericId withNumericId = new WithNumericId();
		temp.save(withNumericId);

		verify(opsMock, times(1)).insert(eq(withNumericId));
	}

	@Test // DATACMNS-525
	void testDoubleSave() {

		Foo foo = new Foo("one");

		repo.save(foo);

		foo.id = "1";
		repo.save(foo);
		verify(opsMock, times(1)).insert(eq(foo));
		verify(opsMock, times(1)).update(eq(foo.getId()), eq(foo));
	}

	@Test // DATACMNS-525
	void multipleSave() {

		Foo one = new Foo("one");
		Foo two = new Foo("two");

		repo.saveAll(Arrays.asList(one, two));
		verify(opsMock, times(1)).insert(eq(one));
		verify(opsMock, times(1)).insert(eq(two));
	}

	@Test // DATACMNS-525
	void deleteEntity() {

		Foo one = new Foo("one");
		one.id = "1";
		repo.save(one);
		repo.delete(one);

		verify(opsMock, times(1)).delete(eq(one));
	}

	@Test // DATACMNS-525
	void deleteById() {

		repo.deleteById("one");

		verify(opsMock, times(1)).delete(eq("one"), eq(Foo.class));
	}

	@Test // DATAKV-330
	void deleteAllById() {

		repo.deleteAllById(Arrays.asList("one", "two"));

		verify(opsMock, times(1)).delete(eq("one"), eq(Foo.class));
		verify(opsMock, times(1)).delete(eq("two"), eq(Foo.class));
	}

	@Test // DATACMNS-525
	void deleteAll() {

		repo.deleteAll();

		verify(opsMock, times(1)).delete(eq(Foo.class));
	}

	@Test // DATACMNS-525
	@SuppressWarnings("unchecked")
	void findAllIds() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.empty());
		repo.findAllById(Arrays.asList("one", "two", "three"));

		verify(opsMock, times(3)).findById(anyString(), eq(Foo.class));
	}

	@Test // DATAKV-186
	@SuppressWarnings("unchecked")
	void existsByIdReturnsFalseForEmptyOptional() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.empty());
		assertThat(repo.existsById("one")).isFalse();
	}

	@Test // DATAKV-186
	@SuppressWarnings("unchecked")
	void existsByIdReturnsTrueWhenOptionalValuePresent() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.of(new Foo()));
		assertThat(repo.existsById("one")).isTrue();
	}

	@Test // DATACMNS-525
	void findAllWithPageableShouldDelegateToOperationsCorrectlyWhenPageableDoesNotContainSort() {

		repo.findAll(PageRequest.of(10, 15));

		verify(opsMock, times(1)).findInRange(eq(150L), eq(15), eq(Sort.unsorted()), eq(Foo.class));
	}

	@Test // DATACMNS-525
	void findAllWithPageableShouldDelegateToOperationsCorrectlyWhenPageableContainsSort() {

		Sort sort = Sort.by("for", "bar");
		repo.findAll(PageRequest.of(10, 15, sort));

		verify(opsMock, times(1)).findInRange(eq(150L), eq(15), eq(sort), eq(Foo.class));
	}

	@Test // DATACMNS-525
	void findAllShouldFallbackToFindAllOfWhenGivenNullPageable() {

		repo.findAll(Pageable.unpaged());

		verify(opsMock, times(1)).findAll(eq(Foo.class));
	}

	@SuppressWarnings("unchecked")
	private <T, S> EntityInformation<T, S> getEntityInformationFor(Class<T> type) {

		PersistentEntity<T, ?> requiredPersistentEntity = (PersistentEntity<T, ?>) context
				.getRequiredPersistentEntity(type);

		return new PersistentEntityInformation<>(requiredPersistentEntity);
	}

	static class Foo {

		private @Id String id;
		private Long longValue;
		private String name;
		private Bar bar;

		Foo(String name) {
			this.name = name;
		}

		public Foo() {}

		public String getId() {
			return this.id;
		}

		public Long getLongValue() {
			return this.longValue;
		}

		public String getName() {
			return this.name;
		}

		public Bar getBar() {
			return this.bar;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setBar(Bar bar) {
			this.bar = bar;
		}
	}

	private static class Bar {

		private String bar;

		public String getBar() {
			return this.bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}
	}

	@Persistent
	static class WithNumericId {

		@Id Integer id;
	}
}
