/*
 * Copyright 2014-2019 the original author or authors.
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

import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.map.AbstractRepositoryUnitTests.PersonRepository;

/**
 * Unit tests for {@link SimpleKeyValueRepository}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class SimpleKeyValueRepositoryUnitTests extends AbstractRepositoryUnitTests<PersonRepository> {

	protected PersonRepository getRepository(KeyValueRepositoryFactory factory) {
		return factory.getRepository(PersonRepository.class);
	}
}
