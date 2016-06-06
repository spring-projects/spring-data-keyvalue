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
package org.springframework.data.map;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.CachingKeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.map.AbstractRepositoryUnitTests.PersonRepository;

/**
 * Unit tests for {@link SimpleKeyValueRepository} using {@link CachingKeyValuePartTreeQuery} and {@link SpelQueryCreator}.
 * 
 * @author Mark Paluch
 */
public class CachingQuerySimpleKeyValueRepositoryUnitTests extends AbstractRepositoryUnitTests<PersonRepository> {

	@Override
	protected KeyValueRepositoryFactory createKeyValueRepositoryFactory(KeyValueOperations operations) {
		return new KeyValueRepositoryFactory(operations, SpelQueryCreator.class, CachingKeyValuePartTreeQuery.class);
	}

	@Override
	protected PersonRepository getRepository(KeyValueRepositoryFactory factory) {
		return factory.getRepository(PersonRepository.class);
	}
}
