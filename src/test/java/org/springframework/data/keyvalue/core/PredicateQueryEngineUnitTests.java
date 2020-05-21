/*
 * Copyright 2015-2020 the original author or authors.
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
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;

/**
 * Unit tests for {@link PredicateQueryEngine}.
 *
 * @author Marcel Overdijk
 */
@RunWith(MockitoJUnitRunner.class)
public class PredicateQueryEngineUnitTests {

	static final Person BOB_WITH_FIRSTNAME = new Person("bob", 30);
	static final Person MIKE_WITHOUT_FIRSTNAME = new Person(null, 25);

	@Mock
    KeyValueAdapter adapter;

	PredicateQueryEngine engine;

	Iterable<Person> people = Arrays.asList(BOB_WITH_FIRSTNAME, MIKE_WITHOUT_FIRSTNAME);

	@Before
	public void setUp() {
		engine = new PredicateQueryEngine();
		engine.registerAdapter(adapter);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void queriesEntitiesWithGivenPredicate() {

		doReturn(people).when(adapter).getAllOf(anyString());

		Predicate<Person> criteria = p -> "bob".equals(p.getFirstname());
		Collection result = engine.execute(criteria, null, -1, -1, anyString());
		assertThat(result).containsExactly(BOB_WITH_FIRSTNAME);
	}

	@Test
	public void countsEntitiesWithGivenPredicate() {

		doReturn(people).when(adapter).getAllOf(anyString());

		Predicate<Person> criteria = p -> "bob".equals(p.getFirstname());
		assertThat(engine.count(criteria, anyString())).isEqualTo(1L);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void queriesEntitiesWithGivenPredicateAndSortAsc() {

		doReturn(people).when(adapter).getAllOf(anyString());

		Predicate<Person> criteria = p -> "bob".equals(p.getFirstname());
		Collection result = engine.execute(criteria, null, -1, -1, anyString());
		assertThat(result).containsExactly(BOB_WITH_FIRSTNAME);
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
	}
}
