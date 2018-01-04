/*
 * Copyright 2015-2018 the original author or authors.
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
package org.springframework.data.keyvalue.core;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Unit tests for {@link SpelQueryEngine}.
 *
 * @author Martin Macko
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class SpelQueryEngineUnitTests {

	static final Person BOB_WITH_FIRSTNAME = new Person("bob", 30);
	static final Person MIKE_WITHOUT_FIRSTNAME = new Person(null, 25);

	@Mock KeyValueAdapter adapter;

	SpelQueryEngine engine;

	Iterable<Person> people = Arrays.asList(BOB_WITH_FIRSTNAME, MIKE_WITHOUT_FIRSTNAME);

	@Before
	public void setUp() {

		engine = new SpelQueryEngine();
		engine.registerAdapter(adapter);
	}

	@Test // DATAKV-114
	@SuppressWarnings("unchecked")
	public void queriesEntitiesWithNullProperty() throws Exception {

		doReturn(people).when(adapter).getAllOf(anyString());

		assertThat(engine.execute(createQueryForMethodWithArgs("findByFirstname", "bob"), null, -1, -1, anyString()),
				contains(BOB_WITH_FIRSTNAME));
	}

	@Test // DATAKV-114
	public void countsEntitiesWithNullProperty() throws Exception {

		doReturn(people).when(adapter).getAllOf(anyString());

		assertThat(engine.count(createQueryForMethodWithArgs("findByFirstname", "bob"), anyString()), is(1L));
	}

	private static SpelCriteria createQueryForMethodWithArgs(String methodName, Object... args) throws Exception {

		List<Class<?>> types = new ArrayList<>(args.length);

		for (Object arg : args) {
			types.add(arg.getClass());
		}

		Method method = PersonRepository.class.getMethod(methodName, types.toArray(new Class<?>[types.size()]));
		RepositoryMetadata metadata = mock(RepositoryMetadata.class);
		doReturn(method.getReturnType()).when(metadata).getReturnedDomainClass(method);

		PartTree partTree = new PartTree(method.getName(), method.getReturnType());
		SpelQueryCreator creator = new SpelQueryCreator(partTree, new ParametersParameterAccessor(
				new QueryMethod(method, metadata, new SpelAwareProxyProjectionFactory()).getParameters(), args));

		return new SpelCriteria(creator.createQuery().getCriteria(), new StandardEvaluationContext(args));
	}

	interface PersonRepository {
		Person findByFirstname(String firstname);
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
