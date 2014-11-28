/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.map.repository.config;

import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration tests for {@link MapRepositoriesRegistrar} with complete defaulting.
 * 
 * @author Christoph Strobl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MapRepositoryRegistrarWithFullDefaultingIntegrationTests {

	@Configuration
	@EnableMapRepositories(considerNestedRepositories = true)
	static class Config {

	}

	@Autowired PersonRepository repo;

	/**
	 * @see DATAKV-86
	 */
	@Test
	public void shouldEnableMapRepositoryCorrectly() {
		assertThat(repo, notNullValue());
	}

	static class Person {

		@Id String id;
		String firstname;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

	}

	static interface PersonRepository extends CrudRepository<Person, String> {

		List<Person> findByFirstname(String firstname);

	}
}
