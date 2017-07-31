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
package org.springframework.data.keyvalue.repository;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;

/**
 * @author Christoph Strobl
 * @author Eugene Nikiforov
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleKeyValueRepositoryUnitTests {

	private SimpleKeyValueRepository<Foo, String> repo;
	private @Mock KeyValueOperations opsMock;

	@Before
	public void setUp() {

		ReflectionEntityInformation<Foo, String> ei = new ReflectionEntityInformation<>(Foo.class);
		repo = new SimpleKeyValueRepository<>(ei, opsMock);
	}

	@Test // DATACMNS-525
	public void saveNewWithNumericId() {

		ReflectionEntityInformation<WithNumericId, Integer> ei = new ReflectionEntityInformation<>(WithNumericId.class);
		SimpleKeyValueRepository<WithNumericId, Integer> temp = new SimpleKeyValueRepository<>(ei, opsMock);

		WithNumericId withNumericId = new WithNumericId();
		temp.save(withNumericId);

		verify(opsMock, times(1)).insert(eq(withNumericId));
	}

	@Test // DATACMNS-525
	public void testDoubleSave() {

		Foo foo = new Foo("one");

		repo.save(foo);

		foo.id = "1";
		repo.save(foo);
		verify(opsMock, times(1)).insert(eq(foo));
		verify(opsMock, times(1)).update(eq(foo.getId()), eq(foo));
	}

	@Test // DATACMNS-525
	public void multipleSave() {

		Foo one = new Foo("one");
		Foo two = new Foo("two");

		repo.saveAll(Arrays.asList(one, two));
		verify(opsMock, times(1)).insert(eq(one));
		verify(opsMock, times(1)).insert(eq(two));
	}

	@Test // DATACMNS-525
	public void deleteEntity() {

		Foo one = new Foo("one");
		one.id = "1";
		repo.save(one);
		repo.delete(one);

		verify(opsMock, times(1)).delete(eq(one.getId()), eq(Foo.class));
	}

	@Test // DATACMNS-525
	public void deleteById() {

		repo.deleteById("one");

		verify(opsMock, times(1)).delete(eq("one"), eq(Foo.class));
	}

	@Test // DATACMNS-525
	public void deleteAll() {

		repo.deleteAll();

		verify(opsMock, times(1)).delete(eq(Foo.class));
	}

	@Test // DATACMNS-525
	@SuppressWarnings("unchecked")
	public void findAllIds() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.empty());
		repo.findAllById(Arrays.asList("one", "two", "three"));

		verify(opsMock, times(3)).findById(anyString(), eq(Foo.class));
	}

	@Test // DATAKV-186
	@SuppressWarnings("unchecked")
	public void existsByIdReturnsFalseForEmptyOptional() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.empty());
		assertThat(repo.existsById("one"), is(false));
	}

	@Test // DATAKV-186
	@SuppressWarnings("unchecked")
	public void existsByIdReturnsTrueWhenOptionalValuePresent() {

		when(opsMock.findById(any(), any(Class.class))).thenReturn(Optional.of(new Foo()));
		assertTrue(repo.existsById("one"));
	}

	@Test // DATACMNS-525
	public void findAllWithPageableShouldDelegateToOperationsCorrectlyWhenPageableDoesNotContainSort() {

		repo.findAll(PageRequest.of(10, 15));

		verify(opsMock, times(1)).findInRange(eq(150L), eq(15), eq(Sort.unsorted()), eq(Foo.class));
	}

	@Test // DATACMNS-525
	public void findAllWithPageableShouldDelegateToOperationsCorrectlyWhenPageableContainsSort() {

		Sort sort = Sort.by("for", "bar");
		repo.findAll(PageRequest.of(10, 15, sort));

		verify(opsMock, times(1)).findInRange(eq(150L), eq(15), eq(sort), eq(Foo.class));
	}

	@Test // DATACMNS-525
	public void findAllShouldFallbackToFindAllOfWhenGivenNullPageable() {

		repo.findAll((Pageable) null);

		verify(opsMock, times(1)).findAll(eq(Foo.class));
	}

	@Data
	@NoArgsConstructor
	static class Foo {

		private @Id String id;
		private Long longValue;
		private String name;
		private Bar bar;

		public Foo(String name) {
			this.name = name;
		}
	}

	@Data
	static class Bar {

		private String bar;
	}

	@Persistent
	static class WithNumericId {

		@Id Integer id;
	}
}
