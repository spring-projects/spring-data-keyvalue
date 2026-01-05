/*
 * Copyright 2014-present the original author or authors.
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
package org.springframework.data.map.repository.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Integration tests for {@link MapRepositoriesRegistrar} with complete defaulting.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class MapRepositoryRegistrarWithTemplateDefinitionIntegrationTests {

	@Configuration
	@EnableMapRepositories(considerNestedRepositories = true)
	static class Config {

		@Bean
		public KeyValueOperations keyValueTemplate() {
			return new KeyValueTemplate(new MapKeyValueAdapter());
		}
	}

	@Autowired PersonRepository repo;

	@Test // DATACMNS-525
	void shouldEnableMapRepositoryCorrectly() {
		assertThat(repo).isNotNull();
	}

	static class Person {

		@Id String id;
		String firstname;

		public String getId() {
			return this.id;
		}

		public String getFirstname() {
			return this.firstname;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}
	}

	interface PersonRepository extends CrudRepository<Person, String> {

		List<Person> findByFirstname(String firstname);
	}
}
