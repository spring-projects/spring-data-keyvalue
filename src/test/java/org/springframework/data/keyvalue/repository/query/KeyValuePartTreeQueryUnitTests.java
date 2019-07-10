/*
 * Copyright 2015-2019 the original author or authors.
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
package org.springframework.data.keyvalue.repository.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.Person;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.SpelCriteria;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyValuePartTreeQueryUnitTests {

	@Mock KeyValueOperations kvOpsMock;
	@Mock RepositoryMetadata metadataMock;
	@Mock ProjectionFactory projectionFactoryMock;

	@Test // DATAKV-115
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void spelExpressionAndContextShouldNotBeReused() throws NoSuchMethodException, SecurityException {

		when(metadataMock.getDomainType()).thenReturn((Class) Person.class);
		when(metadataMock.getReturnedDomainClass(any(Method.class))).thenReturn((Class) Person.class);

		QueryMethod qm = new QueryMethod(Repo.class.getMethod("findByFirstname", String.class), metadataMock,
				projectionFactoryMock);

		KeyValuePartTreeQuery query = new KeyValuePartTreeQuery(qm, QueryMethodEvaluationContextProvider.DEFAULT, kvOpsMock,
				SpelQueryCreator.class);

		Object[] args = new Object[] { "foo" };

		Object first = query.prepareQuery(args).getCriteria();
		Object second = query.prepareQuery(args).getCriteria();

		assertThat(first).isNotSameAs(second);
	}

	@Test // DATAKV-142
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldApplyPageableParameterToCollectionQuery() throws SecurityException, NoSuchMethodException {

		when(metadataMock.getDomainType()).thenReturn((Class) Person.class);
		when(metadataMock.getReturnedDomainClass(any(Method.class))).thenReturn((Class) Person.class);

		QueryMethod qm = new QueryMethod(Repo.class.getMethod("findBy", Pageable.class), metadataMock,
				projectionFactoryMock);

		KeyValuePartTreeQuery partTreeQuery = new KeyValuePartTreeQuery(qm, QueryMethodEvaluationContextProvider.DEFAULT,
				kvOpsMock, SpelQueryCreator.class);

		KeyValueQuery<?> query = partTreeQuery.prepareQuery(new Object[] { PageRequest.of(2, 3) });

		assertThat(query.getOffset()).isEqualTo(6L);
		assertThat(query.getRows()).isEqualTo(3);
	}

	@Test // DATAKV-142
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldApplyDerivedMaxResultsToQuery() throws SecurityException, NoSuchMethodException {

		when(metadataMock.getDomainType()).thenReturn((Class) Person.class);
		when(metadataMock.getReturnedDomainClass(any(Method.class))).thenReturn((Class) Person.class);

		QueryMethod qm = new QueryMethod(Repo.class.getMethod("findTop3By"), metadataMock, projectionFactoryMock);

		KeyValuePartTreeQuery partTreeQuery = new KeyValuePartTreeQuery(qm, QueryMethodEvaluationContextProvider.DEFAULT,
				kvOpsMock, SpelQueryCreator.class);

		KeyValueQuery<?> query = partTreeQuery.prepareQuery(new Object[] {});

		assertThat(query.getRows()).isEqualTo(3);
	}

	@Test // DATAKV-142
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldApplyDerivedMaxResultsToQueryWithParameters() throws SecurityException, NoSuchMethodException {

		when(metadataMock.getDomainType()).thenReturn((Class) Person.class);
		when(metadataMock.getReturnedDomainClass(any(Method.class))).thenReturn((Class) Person.class);

		QueryMethod qm = new QueryMethod(Repo.class.getMethod("findTop3ByFirstname", String.class), metadataMock,
				projectionFactoryMock);

		KeyValuePartTreeQuery partTreeQuery = new KeyValuePartTreeQuery(qm, QueryMethodEvaluationContextProvider.DEFAULT,
				kvOpsMock, SpelQueryCreator.class);

		KeyValueQuery<?> query = partTreeQuery.prepareQuery(new Object[] { "firstname" });

		assertThat(query.getCriteria()).isInstanceOf(SpelCriteria.class);
		assertThat(((SpelCriteria) query.getCriteria()).getExpression().getExpressionString())
				.isEqualTo("#it?.firstname?.equals([0])");
		assertThat(query.getRows()).isEqualTo(3);
	}

	interface Repo {

		List<Person> findByFirstname(String firstname);

		List<Person> findBy(Pageable page);

		List<Person> findTop3By();

		List<Person> findTop3ByFirstname(String firstname);
	}
}
