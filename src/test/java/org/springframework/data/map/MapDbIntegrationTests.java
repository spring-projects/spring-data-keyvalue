/*
 * Copyright 2025-present the original author or authors.
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
package org.springframework.data.map;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Example for MapDB integration testing the repository support through {@link KeySpaceStore}.
 *
 * @author Mark Paluch
 */
@SpringJUnitConfig
class MapDbIntegrationTests {

	@Configuration
	@EnableMapRepositories(considerNestedRepositories = true, keySpaceStoreRef = "store")
	static class TestConfiguration {

		@Bean
		DB db() {
			return DBMaker.heapDB().make();
		}

		@Bean
		KeySpaceStore store(DB db) {

			return new KeySpaceStore() {

				@Override
				public Map<Object, Object> getKeySpace(String keyspace) {
					return db.hashMap(keyspace, Serializer.JAVA, Serializer.JAVA).createOrOpen();
				}

				@Override
				public void clear() {
					db.getStore().getAllRecids().forEachRemaining(it -> db.getStore().delete(it, Serializer.JAVA));
				}

			};
		}

	}

	@Autowired PersonRepository personRepository;
	@Autowired DB db;

	@Test
	void shouldStoreEntriesInMapDb() {

		Person walter = personRepository.save(new Person("Walter", "White"));
		personRepository.save(new Person("Skyler", "White"));
		personRepository.save(new Person("Flynn", "White"));

		assertThat(personRepository.countByLastname("White")).isEqualTo(3);

		HTreeMap<String, Person> backingMap = db.hashMap(Person.class.getName(), Serializer.JAVA, Serializer.JAVA)
				.createOrOpen();

		assertThat(backingMap.size()).isEqualTo(3);
		assertThat(backingMap).containsEntry(walter.id, walter);
	}

	interface PersonRepository extends KeyValueRepository<Person, String> {

		long countByLastname(String lastname);
	}

	static class Person {

		@Id String id;
		String firstname;
		String lastname;

		Person(String firstname, String lastname) {
			this.firstname = firstname;
			this.lastname = lastname;
		}

	}

}
