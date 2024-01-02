/*
 * Copyright 2014-2024 the original author or authors.
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
import org.springframework.data.map.QuerydslKeyValuePredicateExecutorUnitTests.QPersonRepository;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.util.Streamable;

/**
 * Unit tests for {@link org.springframework.data.keyvalue.repository.support.QuerydslKeyValuePredicateExecutor}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
class QuerydslKeyValuePredicateExecutorUnitTests extends AbstractRepositoryUnitTests<QPersonRepository> {

	@BeforeEach
	void setUp() {
		repository.saveAll(LENNISTERS);
	}

	@Test // DATACMNS-525
	void findOneIsExecutedCorrectly() {

		Optional<Person> result = repository.findOne(QPerson.person.firstname.eq(CERSEI.getFirstname()));
		assertThat(result).hasValue(CERSEI);
	}

	@Test // DATACMNS-525
	void findAllIsExecutedCorrectly() {

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()));
		assertThat(result).contains(CERSEI, JAIME);
	}

	@Test // DATACMNS-525
	void findWithPaginationWorksCorrectly() {

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

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				QPerson.person.firstname.desc());

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATACMNS-525
	void findAllUsingPageableWithSortWorksCorrectly() {

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, Direction.DESC, "firstname"));

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATACMNS-525
	void findAllUsingPagableWithQSortWorksCorrectly() {

		Iterable<Person> result = repository.findAll(QPerson.person.age.eq(CERSEI.getAge()),
				PageRequest.of(0, 10, new QSort(QPerson.person.firstname.desc())));

		assertThat(result).containsExactly(JAIME, CERSEI);
	}

	@Test // DATAKV-90
	void findAllWithOrderSpecifierWorksCorrectly() {

		Iterable<Person> result = repository.findAll(new QSort(QPerson.person.firstname.desc()));

		assertThat(result).containsExactly(TYRION, JAIME, CERSEI);
	}

	@Test // DATAKV-90, DATAKV-197
	void findAllShouldRequireSort() {
		assertThatIllegalArgumentException().isThrownBy(() -> repository.findAll((QSort) null));
	}

	@Test // DATAKV-90, DATAKV-197
	void findAllShouldAllowUnsortedFindAll() {

		Iterable<Person> result = repository.findAll(Sort.unsorted());

		assertThat(result).contains(TYRION, JAIME, CERSEI);
	}

	@Test // DATAKV-95
	void executesExistsCorrectly() {
		assertThat(repository.exists(QPerson.person.age.eq(CERSEI.getAge()))).isTrue();
	}

	@Test // DATAKV-96
	void shouldSupportFindAllWithPredicateAndSort() {

		List<Person> users = Streamable.of(repository.findAll(person.age.gt(0), Sort.by(Direction.ASC, "firstname")))
				.toList();

		assertThat(users).hasSize(3);
		assertThat(users.get(0).getFirstname()).isEqualTo(CERSEI.getFirstname());
		assertThat(users.get(2).getFirstname()).isEqualTo(TYRION.getFirstname());
		assertThat(users).contains(CERSEI, JAIME, TYRION);
	}

	@Test // DATAKV-179
	void throwsExceptionIfMoreThanOneResultIsFound() {

		assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class) //
				.isThrownBy(() -> repository.findOne(person.firstname.contains("e")));
	}

	@Test // GH-397
	void findByShouldReturnFirst() {

		Person first = repository.findBy(QPerson.person.firstname.eq("tyrion"),
				FluentQuery.FetchableFluentQuery::firstValue);

		assertThat(first).isEqualTo(TYRION);

		first = repository.findBy(QPerson.person.firstname.eq("foo"), Function.identity()).firstValue();

		assertThat(first).isNull();
	}

	@Test // GH-397
	void findByShouldReturnOne() {

		assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class)
				.isThrownBy(() -> repository.findBy(QPerson.person.firstname.ne("foo"), FluentQuery.FetchableFluentQuery::one));

		Person one = repository.findBy(QPerson.person.firstname.eq("tyrion"), FluentQuery.FetchableFluentQuery::oneValue);

		assertThat(one).isEqualTo(TYRION);
	}

	@Test // GH-397
	void findByShouldReturnFirstWithProjection() {

		PersonProjection interfaceProjection = repository.findBy(QPerson.person.firstname.eq("tyrion"),
				it -> it.as(PersonProjection.class).firstValue());
		assertThat(interfaceProjection.getFirstname()).isEqualTo("tyrion");

		PersonDto dto = repository.findBy(QPerson.person.firstname.eq("tyrion"), it -> it.as(PersonDto.class).firstValue());
		assertThat(dto.getFirstname()).isEqualTo("tyrion");
	}

	@Test // GH-397
	void findByShouldReturnOneWithProjection() {

		PersonProjection interfaceProjection = repository.findBy(QPerson.person.firstname.eq("tyrion"),
				it -> it.as(PersonProjection.class).oneValue());
		assertThat(interfaceProjection.getFirstname()).isEqualTo("tyrion");

		PersonDto dto = repository.findBy(QPerson.person.firstname.eq("tyrion"), it -> it.as(PersonDto.class).oneValue());
		assertThat(dto.getFirstname()).isEqualTo("tyrion");
	}

	@Test // GH-397
	void findByShouldReturnAll() {

		List<Person> all = repository.findBy(QPerson.person.firstname.eq("tyrion"), FluentQuery.FetchableFluentQuery::all);

		assertThat(all).contains(TYRION);
	}

	@Test // GH-397
	void findByShouldReturnAllSorted() {

		List<Person> all = repository.findBy(QPerson.person.firstname.ne("foo"),
				q -> q.sortBy(Sort.by(Direction.ASC, "firstname")).all());

		assertThat(all).containsSequence(CERSEI, JAIME, TYRION);

		all = repository.findBy(QPerson.person.firstname.ne("foo"),
				q -> q.sortBy(Sort.by(Direction.DESC, "firstname")).all());

		assertThat(all).containsSequence(TYRION, JAIME, CERSEI);
	}

	@Test // GH-397
	void findByShouldReturnAllWithProjection() {

		Stream<PersonProjection> all = repository.findBy(QPerson.person.firstname.eq("tyrion"),
				q -> q.as(PersonProjection.class).stream());

		assertThat(all).hasOnlyElementsOfType(PersonProjection.class);
	}

	@Test // GH-397
	void findByShouldReturnPage() {

		Page<PersonProjection> page = repository.findBy(QPerson.person.firstname.ne("foo"),
				it -> it.as(PersonProjection.class).page(PageRequest.of(0, 1, Sort.by("firstname"))));

		assertThat(page.getContent().get(0).getFirstname()).isEqualTo("cersei");
		assertThat(page.getTotalPages()).isEqualTo(3);

		Page<PersonProjection> nextPage = repository.findBy(QPerson.person.firstname.ne("foo"),
				it -> it.as(PersonProjection.class).page(page.nextPageable()));

		assertThat(nextPage.getContent().get(0).getFirstname()).isEqualTo("jaime");
		assertThat(nextPage.getTotalPages()).isEqualTo(3);
	}

	@Test // GH-397
	void findByShouldReturnStream() {

		List<Person> all = repository.findBy(QPerson.person.firstname.eq("tyrion"), FluentQuery.FetchableFluentQuery::all);

		assertThat(all).contains(TYRION);
	}

	@Test // GH-397
	void findByShouldReturnStreamWithProjection() {

		Stream<PersonProjection> all = repository.findBy(QPerson.person.firstname.eq("tyrion"),
				q -> q.as(PersonProjection.class).stream());

		assertThat(all).hasOnlyElementsOfType(PersonProjection.class);
	}

	@Test // GH-397
	void findByShouldReturnCount() {

		long count = repository.findBy(QPerson.person.firstname.ne("foo"), FluentQuery.FetchableFluentQuery::count);

		assertThat(count).isEqualTo(3);
	}

	@Test // GH-397
	void findByShouldReturnExists() {

		boolean exists = repository.findBy(QPerson.person.firstname.eq("tyrion"), FluentQuery.FetchableFluentQuery::exists);
		assertThat(exists).isTrue();

		exists = repository.findBy(QPerson.person.firstname.eq("foo"), FluentQuery.FetchableFluentQuery::exists);
		assertThat(exists).isFalse();
	}

	interface PersonProjection {
		String getFirstname();
	}

	static class PersonDto {

		String firstname;

		public String getFirstname() {
			return this.firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}
	}

	@Override
	protected QPersonRepository getRepository(KeyValueRepositoryFactory factory) {
		return factory.getRepository(QPersonRepository.class);
	}

	interface QPersonRepository extends org.springframework.data.map.AbstractRepositoryUnitTests.PersonRepository,
			QuerydslPredicateExecutor<Person> {}
}
