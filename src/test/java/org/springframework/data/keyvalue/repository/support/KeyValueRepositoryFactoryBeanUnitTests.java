/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.data.keyvalue.repository.support;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

/**
 * Unit tests for {@link KeyValueRepositoryFactoryBean}.
 * 
 * @author Oliver Gierke
 */
public class KeyValueRepositoryFactoryBeanUnitTests {

	public @Rule ExpectedException exception = ExpectedException.none();

	KeyValueRepositoryFactoryBean<?, ?, ?> factoryBean;

	@Before
	public void setUp() {
		this.factoryBean = new KeyValueRepositoryFactoryBean<Repository<Object, Serializable>, Object, Serializable>(
				SampleRepository.class);
	}

	/**
	 * @see DATAKV-123
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullKeyValueOperations() {
		factoryBean.setKeyValueOperations(null);
	}

	/**
	 * @see DATAKV-123
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullQueryCreator() {
		factoryBean.setQueryCreator(null);
	}

	/**
	 * @see DATAKV-123
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsUninitializedInstance() {
		factoryBean.afterPropertiesSet();
	}

	/**
	 * @see DATAKV-123
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void rejectsInstanceWithoutKeyValueOperations() {

		Class<? extends AbstractQueryCreator<?, ?>> creatorType = (Class<? extends AbstractQueryCreator<?, ?>>) mock(
				AbstractQueryCreator.class).getClass();

		factoryBean.setQueryCreator(creatorType);
		factoryBean.afterPropertiesSet();
	}

	/**
	 * @see DATAKV-123
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsInstanceWithoutQueryCreator() {

		factoryBean.setKeyValueOperations(mock(KeyValueOperations.class));
		factoryBean.afterPropertiesSet();
	}

	/**
	 * @see DATAKV-123
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void createsRepositoryFactory() {

		Class<? extends AbstractQueryCreator<?, ?>> creatorType = (Class<? extends AbstractQueryCreator<?, ?>>) mock(
				AbstractQueryCreator.class).getClass();
		Class<? extends RepositoryQuery> queryType = (Class<? extends RepositoryQuery>) mock(KeyValuePartTreeQuery.class)
				.getClass();

		factoryBean.setQueryCreator(creatorType);
		factoryBean.setKeyValueOperations(mock(KeyValueOperations.class));
		factoryBean.setQueryType(queryType);

		assertThat(factoryBean.createRepositoryFactory(), is(notNullValue()));
	}

	/**
	 * @see DATAKV-112
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullQueryType() {
		factoryBean.setQueryType(null);
	}

	interface SampleRepository extends Repository<Object, Serializable> {}
}
