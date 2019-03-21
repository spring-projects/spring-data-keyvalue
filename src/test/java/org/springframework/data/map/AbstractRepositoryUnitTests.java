/*
 * Copyright 2014-2015 the original author or authors.
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
package org.springframework.data.map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.keyvalue.Person;
import org.springframework.data.keyvalue.QPerson;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.repository.CrudRepository;

/**
 * Base class for test cases for repository implementations.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public abstract class AbstractRepositoryUnitTests<T extends AbstractRepositoryUnitTests.PersonRepository> {

	protected static final Person CERSEI = new Person("cersei", 19);
	protected static final Person JAIME = new Person("jaime", 19);
	protected static final Person TYRION = new Person("tyrion", 17);

	protected static List<Person> LENNISTERS = Arrays.asList(CERSEI, JAIME, TYRION);

	protected final QPerson person = QPerson.person;

	protected T repository;
	protected KeyValueRepositoryFactory factory;

	@Before
	public void setup() {

		KeyValueOperations operations = new KeyValueTemplate(new MapKeyValueAdapter());
		KeyValueRepositoryFactory keyValueRepositoryFactory = new KeyValueRepositoryFactory(operations);

		this.repository = getRepository(keyValueRepositoryFactory);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findBy() {

		repository.save(LENNISTERS);

		assertThat(repository.findByAge(19), hasItems(CERSEI, JAIME));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void combindedFindUsingAnd() {

		repository.save(LENNISTERS);

		assertThat(repository.findByFirstnameAndAge(JAIME.getFirstname(), 19), hasItem(JAIME));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findPage() {

		repository.save(LENNISTERS);

		Page<Person> page = repository.findByAge(19, new PageRequest(0, 1));
		assertThat(page.hasNext(), is(true));
		assertThat(page.getTotalElements(), is(2L));
		assertThat(page.getContent(), IsCollectionWithSize.hasSize(1));

		Page<Person> next = repository.findByAge(19, page.nextPageable());
		assertThat(next.hasNext(), is(false));
		assertThat(next.getTotalElements(), is(2L));
		assertThat(next.getContent(), IsCollectionWithSize.hasSize(1));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findByConnectingOr() {

		repository.save(LENNISTERS);

		assertThat(repository.findByAgeOrFirstname(19, TYRION.getFirstname()), hasItems(CERSEI, JAIME, TYRION));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void singleEntityExecution() {

		repository.save(LENNISTERS);

		assertThat(repository.findByAgeAndFirstname(TYRION.getAge(), TYRION.getFirstname()), is(TYRION));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findAllShouldRespectSort() {

		repository.save(LENNISTERS);

		assertThat(
				repository.findAll(new Sort(new Sort.Order(Direction.ASC, "age"), new Sort.Order(Direction.DESC, "firstname"))),
				contains(TYRION, JAIME, CERSEI));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void derivedFinderShouldRespectSort() {

		repository.save(LENNISTERS);

		List<Person> result = repository.findByAgeGreaterThanOrderByAgeAscFirstnameDesc(2);

		assertThat(result, contains(TYRION, JAIME, CERSEI));
	}

	protected abstract T getRepository(KeyValueRepositoryFactory factory);

	public static interface PersonRepository extends CrudRepository<Person, String>, KeyValueRepository<Person, String> {

		List<Person> findByAge(int age);

		List<Person> findByFirstname(String firstname);

		List<Person> findByFirstnameAndAge(String firstname, int age);

		Page<Person> findByAge(int age, Pageable page);

		List<Person> findByAgeOrFirstname(int age, String firstname);

		Person findByAgeAndFirstname(int age, String firstname);

		List<Person> findByAgeGreaterThanOrderByAgeAscFirstnameDesc(int age);

	}

}
