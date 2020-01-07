/*
 * Copyright 2014-2020 the original author or authors.
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
import static org.springframework.data.keyvalue.repository.support.KeyValueQuerydslUtils.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.keyvalue.Person;
import org.springframework.data.keyvalue.QPerson;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * Unit tests for {@link KeyValueQuerydslUtils}.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueQuerydslUtilsUnitTests {

	private EntityPath<Person> path;
	private PathBuilder<Person> builder;

	@Before
	public void setUp() {

		this.path = SimpleEntityPathResolver.INSTANCE.createPath(Person.class);
		this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
	}

	@Test // DATACMNS-525
	public void toOrderSpecifierThrowsExceptioOnNullPathBuilder() {
		assertThatIllegalArgumentException().isThrownBy(() -> toOrderSpecifier(Sort.by("firstname"), null));
	}

	@Test // DATACMNS-525, DATAKV-197
	public void toOrderSpecifierReturnsEmptyArrayWhenSortIsUnsorted() {
		assertThat(toOrderSpecifier(Sort.unsorted(), builder)).hasSize(0);
	}

	@Test // DATACMNS-525
	public void toOrderSpecifierConvertsSimpleAscSortCorrectly() {

		Sort sort = Sort.by(Direction.ASC, "firstname");

		OrderSpecifier<?>[] specifiers = toOrderSpecifier(sort, builder);

		assertThat(specifiers).containsExactly(QPerson.person.firstname.asc());
	}

	@Test // DATACMNS-525
	public void toOrderSpecifierConvertsSimpleDescSortCorrectly() {

		Sort sort = Sort.by(Direction.DESC, "firstname");

		OrderSpecifier<?>[] specifiers = toOrderSpecifier(sort, builder);

		assertThat(specifiers).containsExactly(QPerson.person.firstname.desc());
	}

	@Test // DATACMNS-525
	public void toOrderSpecifierConvertsSortCorrectlyAndRetainsArgumentOrder() {

		Sort sort = Sort.by(Direction.DESC, "firstname").and(Sort.by(Direction.ASC, "age"));

		OrderSpecifier<?>[] specifiers = toOrderSpecifier(sort, builder);

		assertThat(specifiers).containsExactly(QPerson.person.firstname.desc(), QPerson.person.age.asc());
	}

	@Test // DATACMNS-525
	public void toOrderSpecifierConvertsSortWithNullHandlingCorrectly() {

		Sort sort = Sort.by(new Sort.Order(Direction.DESC, "firstname", NullHandling.NULLS_LAST));

		OrderSpecifier<?>[] specifiers = toOrderSpecifier(sort, builder);

		assertThat(specifiers).containsExactly(QPerson.person.firstname.desc().nullsLast());
	}
}
