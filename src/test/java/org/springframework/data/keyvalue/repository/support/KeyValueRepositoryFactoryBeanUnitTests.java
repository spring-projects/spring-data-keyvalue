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
package org.springframework.data.keyvalue.repository.support;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

/**
 * Unit tests for {@link KeyValueRepositoryFactoryBean}.
 *
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueRepositoryFactoryBeanUnitTests {

	KeyValueRepositoryFactoryBean<?, ?, ?> factoryBean;

	@Before
	public void setUp() {
		this.factoryBean = new KeyValueRepositoryFactoryBean<Repository<Object, Object>, Object, Object>(
				SampleRepository.class);
	}

	@Test // DATAKV-123
	public void rejectsNullKeyValueOperations() {
		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.setKeyValueOperations(null));
	}

	@Test // DATAKV-123
	public void rejectsNullQueryCreator() {
		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.setQueryCreator(null));
	}

	@Test // DATAKV-123
	public void rejectsUninitializedInstance() {
		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.afterPropertiesSet());
	}

	@SuppressWarnings("unchecked")
	@Test // DATAKV-123
	public void rejectsInstanceWithoutKeyValueOperations() {

		Class<? extends AbstractQueryCreator<?, ?>> creatorType = (Class<? extends AbstractQueryCreator<?, ?>>) mock(
				AbstractQueryCreator.class).getClass();

		factoryBean.setQueryCreator(creatorType);

		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.afterPropertiesSet());
	}

	@Test // DATAKV-123
	public void rejectsInstanceWithoutQueryCreator() {

		factoryBean.setKeyValueOperations(mock(KeyValueOperations.class));
		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.afterPropertiesSet());
	}

	@Test // DATAKV-123
	@SuppressWarnings("unchecked")
	public void createsRepositoryFactory() {

		Class<? extends AbstractQueryCreator<?, ?>> creatorType = (Class<? extends AbstractQueryCreator<?, ?>>) mock(
				AbstractQueryCreator.class).getClass();
		Class<? extends RepositoryQuery> queryType = mock(KeyValuePartTreeQuery.class).getClass();

		factoryBean.setQueryCreator(creatorType);
		factoryBean.setKeyValueOperations(mock(KeyValueOperations.class));
		factoryBean.setQueryType(queryType);

		assertThat(factoryBean.createRepositoryFactory()).isNotNull();
	}

	@Test // DATAKV-112
	public void rejectsNullQueryType() {
		assertThatIllegalArgumentException().isThrownBy(() -> factoryBean.setQueryType(null));
	}

	interface SampleRepository extends Repository<Object, Object> {}
}
