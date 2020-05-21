/*
 * Copyright 2014-2020 the original author or authors.
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
package org.springframework.data.keyvalue.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.KeyValueTemplateTests.Foo;
import org.springframework.data.keyvalue.core.PredicateQueryEngineUnitTests.Person;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.map.MapKeyValueAdapter;

/**
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class PredicateQueryEngineKeyValueTemplateTests {

	static final Person BOB_WITH_FIRSTNAME = new Person("bob", 30);
	static final Person MIKE_WITHOUT_FIRSTNAME = new Person(null, 25);
	static final Person WINSTON_WITH_FIRSTNAME = new Person("winston", 35);

	KeyValueTemplate operations;

	@Before
	public void setUp() throws InstantiationException, IllegalAccessException {
		this.operations = new KeyValueTemplate(new MapKeyValueAdapter(new PredicateQueryEngine()));

		operations.insert("1", BOB_WITH_FIRSTNAME);
		operations.insert("2", MIKE_WITHOUT_FIRSTNAME);
		operations.insert("3", WINSTON_WITH_FIRSTNAME);
	}

	@After
	public void tearDown() throws Exception {
		this.operations.destroy();
	}

	@Test
	public void findShouldExecuteQueryCorrectly() {

		Predicate<Person> criteria = p -> p.getFirstname() != null;
		KeyValueQuery<Predicate> query = new KeyValueQuery<>(criteria);

		List<Person> result = (List<Person>) operations.find(query, Person.class);
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(BOB_WITH_FIRSTNAME);
		assertThat(result.get(1)).isEqualTo(WINSTON_WITH_FIRSTNAME);
	}

	@Test
	public void findShouldExecuteQueryWithSortCorrectly() {

		Predicate<Person> criteria = p -> p.getFirstname() != null;
		Sort sort = Sort.by(Direction.DESC, "age");
		KeyValueQuery<Predicate> query = new KeyValueQuery<>(criteria, sort);

		List<Person> result = (List<Person>) operations.find(query, Person.class);
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(WINSTON_WITH_FIRSTNAME);
		assertThat(result.get(1)).isEqualTo(BOB_WITH_FIRSTNAME);
	}

	static class Person {

		@Id String id;
		String firstname;
		int age;

		public Person(String firstname, int age) {

			this.firstname = firstname;
			this.age = age;
		}

		public String getFirstname() {
			return firstname;
		}

		public int getAge() {
			return age;
		}
	}
}
