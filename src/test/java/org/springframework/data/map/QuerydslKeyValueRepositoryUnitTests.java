/*
 * Copyright 2014-2021 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import org.springframework.data.util.Version;

import com.google.common.collect.Lists;

/**
 * Unit tests for {@link QuerydslKeyValueRepository}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public class QuerydslKeyValueRepositoryUnitTests extends AbstractRepositoryUnitTests<QPersonRepository> {

	@BeforeEach
	void before() {
		assumeThat(Version.javaVersion().toString()).startsWith("1.8");
	}

	@Test // DATACMNS-525
	void findOneIsExecutedCorrectly() {

		repository.saveAll(LENNISTERS);

		Optional<Person> result = repository.findOne(QPerson.person.firstname.eq(CERSEI.getFirstname()));
		assertThat(result).hasValue(CERSEI);
	}

	@Test // DATACMNS-525
	void findAllIsExecutedCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()));
		assertThat(result).contains(CERSEI, JAIME);
	}

	@Test // DATACMNS-525
	void findWithPaginationWorksCorrectly() {

		repository.saveAll(LENNISTERS);
		Page<Person> page1 = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()), PageRequest.of(0, 1));

		assertThat(page1.getTotalElements()).isEqualTo(2L);
		assertThat(page1.getContent()).hasSize(1);
		assertThat(page1.hasNext()).isTrue();

		Page<Person> page2 = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()), page1.nextPageable());

		assertThat(page2.getTotalElements()).isEqualTo(2L);
		assertThat(page2.getContent()).hasSize(1);
		assertThat(page2.hasNext()).isFalse();
	}

	@Test // DATACMNS-525
	void findAllUsingOrderSpecifierWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				QPerson.person.firstname.desc());

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATACMNS-525
	void findAllUsingPageableWithSortWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, Direction.DESC, "firstname"));

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATACMNS-525
	void findAllUsingPagableWithQSortWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, new QSort(QPerson.person.firstname.desc())));

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATAKV-90
	void findAllWithOrderSpecifierWorksCorrectly() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(new QSort(QPerson.person.firstname.desc()));

		assertThat(result).containsExactly(TYRION, JAIME, CERSEI);
	}

	@Test // DATAKV-90, DATAKV-197
	void findAllShouldRequireSort() {
		assertThatIllegalArgumentException().isThrownBy(() -> repository.findAll((QSort) null));
	}

	@Test // DATAKV-90, DATAKV-197
	void findAllShouldAllowUnsortedFindAll() {

		repository.saveAll(LENNISTERS);

		Iterable<Person> result = repository.findAll(Sort.unsorted());

		assertThat(result).contains(TYRION, JAIME, CERSEI);
	}

	@Test // DATAKV-95
	void executesExistsCorrectly() {

		repository.saveAll(LENNISTERS);

		assertThat(repository.exists(QPerson.person.age.eq(CERSEI.getAge()))).isTrue();
	}

	@Test // DATAKV-96
	void shouldSupportFindAllWithPredicateAndSort() {

		repository.saveAll(LENNISTERS);

		List<Person> users = Lists.newArrayList(repository.findAll(person.age.gt(0), Sort.by(Direction.ASC, "firstname")));

		assertThat(users).hasSize(3);
		assertThat(users.get(0).getFirstname()).isEqualTo(CERSEI.getFirstname());
		assertThat(users.get(2).getFirstname()).isEqualTo(TYRION.getFirstname());
		assertThat(users).contains(CERSEI, JAIME, TYRION);
	}

	@Test // DATAKV-179
	void throwsExceptionIfMoreThanOneResultIsFound() {

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
