/*
 * Copyright 2016-2021 the original author or authors.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.keyvalue.Person;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.SpelCriteria;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

/**
 * Unit tests for {@link CachingKeyValuePartTreeQuery}.
 *
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingKeyValuePartTreeQueryUnitTests {

	@Mock KeyValueOperations kvOpsMock;
	@Mock RepositoryMetadata metadataMock;
	@Mock ProjectionFactory projectionFactoryMock;

	@Before
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setUp() throws Exception {

		when(metadataMock.getDomainType()).thenReturn((Class) Person.class);
		when(metadataMock.getReturnedDomainClass(any(Method.class))).thenReturn((Class) Person.class);
	}

	@Test // DATAKV-137
	public void cachedSpelExpressionShouldBeReusedWithNewContext() throws NoSuchMethodException, SecurityException {

		QueryMethod qm = new QueryMethod(Repo.class.getMethod("findByFirstname", String.class), metadataMock,
				projectionFactoryMock);

		KeyValuePartTreeQuery query = new CachingKeyValuePartTreeQuery(qm, QueryMethodEvaluationContextProvider.DEFAULT,
				kvOpsMock, SpelQueryCreator.class);

		Object[] args = new Object[] { "foo" };

		SpelCriteria first = (SpelCriteria) query.prepareQuery(args).getCriteria();
		SpelCriteria second = (SpelCriteria) query.prepareQuery(args).getCriteria();

		assertThat(first.getExpression()).isSameAs(second.getExpression());
		assertThat(first.getContext()).isNotSameAs(second.getContext());
	}

	static interface Repo {

		List<Person> findByFirstname(String firstname);
	}
}
