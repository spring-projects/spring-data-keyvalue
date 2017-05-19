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
package org.springframework.data.map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.keyvalue.Person;
import org.springframework.data.keyvalue.QPerson;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.keyvalue.repository.support.QuerydslKeyValueRepository;
import org.springframework.data.map.QuerydslKeyValueRepositoryUnitTests.QPersonRepository;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.google.common.collect.Lists;

/**
 * Unit tests for {@link QuerydslKeyValueRepository}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class QuerydslKeyValueRepositoryUnitTests extends AbstractRepositoryUnitTests<QPersonRepository> {

	@Test // DATACMNS-525
	public void findOneIsExecutedCorrectly() {

		repository.saveAll(LENNISTERS);

		Optional<Person> result = repository.findOne(QPerson.person.firstname.eq(CERSEI.getFirstname()));
		assertThat(result).hasValue(CERSEI);
	}

	@Test // DATACMNS-525
	public void findAllIsExecutedCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()));
		assertThat(result, containsInAnyOrder(CERSEI, JAIME));
	}

	@Test // DATACMNS-525
	public void findWithPaginationWorksCorrectly() {

		repository.saveAll(LENNISTERS);
		Page<Person> page1 = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()), PageRequest.of(0, 1));

		assertThat(page1.getTotalElements(), is(2L));
		assertThat(page1.getContent(), hasSize(1));
		assertThat(page1.hasNext(), is(true));

		Page<Person> page2 = ((QPersonRepository) repository).findAll(QPerson.person.age.eq(CERSEI.getAge()),
				page1.nextPageable());

		assertThat(page2.getTotalElements(), is(2L));
		assertThat(page2.getContent(), hasSize(1));
		assertThat(page2.hasNext(), is(false));
	}

	@Test // DATACMNS-525
	public void findAllUsingOrderSpecifierWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				QPerson.person.firstname.desc());

		assertThat(result, contains(JAIME, CERSEI));
	}

	@Test // DATACMNS-525
	public void findAllUsingPageableWithSortWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, Direction.DESC, "firstname"));

		assertThat(result, contains(JAIME, CERSEI));
	}

	@Test // DATACMNS-525
	public void findAllUsingPagableWithQSortWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, new QSort(QPerson.person.firstname.desc())));

		assertThat(result, contains(JAIME, CERSEI));
	}

	@Test // DATAKV-90
	public void findAllWithOrderSpecifierWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(new QSort(QPerson.person.firstname.desc()));

		assertThat(result, contains(TYRION, JAIME, CERSEI));
	}

	@Test // DATAKV-90
	public void findAllShouldIgnoreNullOrderSpecifier() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll((QSort) null);

		assertThat(result, containsInAnyOrder(TYRION, JAIME, CERSEI));
	}

	@Test // DATAKV-95
	public void executesExistsCorrectly() {

		repository.saveAll(LENNISTERS);

		assertThat(repository.exists(QPerson.person.age.eq(CERSEI.getAge())), is(true));
	}

	@Test // DATAKV-96
	public void shouldSupportFindAllWithPredicateAndSort() {

		repository.saveAll(LENNISTERS);

		List<Person> users = Lists.newArrayList(repository.findAll(person.age.gt(0), Sort.by(Direction.ASC, "firstname")));

		assertThat(users, hasSize(3));
		assertThat(users.get(0).getFirstname(), is(CERSEI.getFirstname()));
		assertThat(users.get(2).getFirstname(), is(TYRION.getFirstname()));
		assertThat(users, hasItems(CERSEI, JAIME, TYRION));
	}

	@Test // DATAKV-179
	public void throwsExceptionIfMoreThanOneResultIsFound() {

		repository.saveAll(LENNISTERS);

		assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class) //
				.isThrownBy(() -> repository.findOne(person.firstname.contains("e")));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.map.SimpleKeyValueRepositoryUnitTests#getRepository(org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory)
	 */
	@Override
	protected QPersonRepository getRepository(KeyValueRepositoryFactory factory) {
		return factory.getRepository(QPersonRepository.class);
	}

	static interface QPersonRepository extends org.springframework.data.map.AbstractRepositoryUnitTests.PersonRepository,
			QuerydslPredicateExecutor<Person> {}
}
