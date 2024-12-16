/*
 * Copyright 2024 the original author or authors.
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
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 * @author Tom Van Wemmel
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractQueryCreatorTestBase<QUERY_CREATOR extends AbstractQueryCreator<KeyValueQuery<CRITERIA>, ?>, CRITERIA> {

	static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

	static final Person RICKON = new Person("rickon", 4);
	static final Person BRAN = new Person("bran", 9)//
			.skinChanger(true).bornAt(Date.from(ZonedDateTime.parse("2013-01-31T06:00:00Z", FORMATTER).toInstant()));
	static final Person ARYA = new Person("arya", 13);
	static final Person ROBB = new Person("robb", 16)//
			.named("stark").bornAt(Date.from(ZonedDateTime.parse("2010-09-20T06:00:00Z", FORMATTER).toInstant()));
	static final Person JON = new Person("jon", 17).named("snow");

	@Mock RepositoryMetadata metadataMock;

	@Test // DATACMNS-525
	void equalsReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void equalsReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(RICKON)).isFalse();
	}

	@Test // GH-603
	void notEqualsReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameNot", BRAN.firstname).against(RICKON)).isTrue();
	}

	@Test // GH-603
	void notEqualsReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameNot", BRAN.firstname).against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	void isTrueAssertedProperlyWhenTrue() {
		assertThat(evaluate("findBySkinChangerIsTrue").against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void isTrueAssertedProperlyWhenFalse() {
		assertThat(evaluate("findBySkinChangerIsTrue").against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	void isFalseAssertedProperlyWhenTrue() {
		assertThat(evaluate("findBySkinChangerIsFalse").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	void isFalseAssertedProperlyWhenFalse() {
		assertThat(evaluate("findBySkinChangerIsFalse").against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	void isNullAssertedProperlyWhenAttributeIsNull() {
		assertThat(evaluate("findByLastnameIsNull").against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void isNullAssertedProperlyWhenAttributeIsNotNull() {
		assertThat(evaluate("findByLastnameIsNull").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void isNotNullFalseTrueWhenAttributeIsNull() {
		assertThat(evaluate("findByLastnameIsNotNull").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	void isNotNullReturnsTrueAttributeIsNotNull() {
		assertThat(evaluate("findByLastnameIsNotNull").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void startsWithReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void startsWithReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	void likeReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameLike", "ob").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void likeReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameLike", "ra").against(ROBB)).isFalse();
	}

	@Test // GH-603
	void notLikeReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameNotLike", "ra").against(ROBB)).isTrue();
	}

	@Test // GH-603
	void notLikeReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameNotLike", "ob").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void endsWithReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameEndingWith", "bb").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void endsWithReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameEndingWith", "an").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void startsWithIgnoreCaseReturnsTrueWhenMatching() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
				.isThrownBy(() -> evaluate("findByFirstnameIgnoreCase", "R").against(ROBB));
	}

	@Test // DATACMNS-525
	void greaterThanReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void greaterThanReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	void afterReturnsTrueForHigherValues() {
		assertThat(evaluate("findByBirthdayAfter", ROBB.birthday).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void afterReturnsFalseForLowerValues() {
		assertThat(evaluate("findByBirthdayAfter", BRAN.birthday).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void greaterThanEaualsReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void greaterThanEqualsReturnsTrueForEqualValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void greaterThanEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	void lessThanReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void lessThanReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	void beforeReturnsTrueForLowerValues() {
		assertThat(evaluate("findByBirthdayBefore", BRAN.birthday).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void beforeReturnsFalseForHigherValues() {
		assertThat(evaluate("findByBirthdayBefore", ROBB.birthday).against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	void lessThanEaualsReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void lessThanEaualsReturnsTrueForEqualValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	void lessThanEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	void betweenEqualsReturnsTrueForValuesInBetween() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(ARYA)).isTrue();
	}

	@Test // DATACMNS-525
	void betweenEqualsReturnsFalseForHigherValues() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(JON)).isFalse();
	}

	@Test // DATACMNS-525
	void betweenEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	void connectByAndReturnsTrueWhenAllPropertiesMatching() {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	void connectByAndReturnsFalseWhenOnlyFewPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	void connectByOrReturnsTrueWhenOnlyFewPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	void connectByOrReturnsTrueWhenAllPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	void regexReturnsTrueWhenMatching() {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	void regexReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(ROBB)).isFalse();
	}

	@Test // DATAKV-169
	void inReturnsMatchCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(ROBB)).isTrue();
	}

	@Test // DATAKV-169
	void inNotMatchingReturnsCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(JON)).isFalse();
	}

	@Test // DATAKV-169
	void inWithNullCompareValuesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(null);

		assertThat(evaluate("findByFirstnameIn", list).against(JON)).isFalse();
	}

	@Test // DATAKV-169
	void inWithNullSourceValuesMatchesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(new PredicateQueryCreatorUnitTests.Person(null, 10)))
				.isFalse();
	}

	@Test // DATAKV-169
	void inMatchesNullValuesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(null);

		boolean contains = list.contains(null);

		assertThat(evaluate("findByFirstnameIn", list).against(new PredicateQueryCreatorUnitTests.Person(null, 10)))
				.isTrue();
	}

	@Test // GH-603
	void notInReturnsMatchCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameNotIn", list).against(JON)).isTrue();
	}

	@Test // GH-603
	void notInNotMatchingReturnsCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameNotIn", list).against(ROBB)).isFalse();
	}

	@Test // GH-603
	void notInWithNullCompareValuesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(null);

		assertThat(evaluate("findByFirstnameNotIn", list).against(JON)).isTrue();
	}

	@Test // GH-603
	void notInWithNullSourceValuesMatchesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameNotIn", list).against(new PredicateQueryCreatorUnitTests.Person(null, 10)))
				.isTrue();
	}

	@Test // GH-603
	void notInMatchesNullValuesCorrectly() {

		List<String> list = new ArrayList<>();
		list.add(null);

		assertThat(evaluate("findByFirstnameNotIn", list).against(new PredicateQueryCreatorUnitTests.Person(null, 10)))
				.isFalse();
	}

	@Test // DATAKV-185
	void noDerivedQueryArgumentsMatchesAlways() {

		assertThat(evaluate("findBy").against(JON)).isTrue();
		assertThat(evaluate("findBy").against(null)).isTrue();
	}

	protected Evaluation evaluate(String methodName, Object... args) {
		try {
			return createEvaluation(createQueryForMethodWithArgs(methodName, args).getCriteria());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Evaluation createEvaluation(CRITERIA criteria);

	protected KeyValueQuery<CRITERIA> createQueryForMethodWithArgs(String methodName, Object... args)
			throws NoSuchMethodException, SecurityException {

		Class<?>[] argTypes = new Class<?>[args.length];
		if (!ObjectUtils.isEmpty(args)) {

			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
		}

		Method method = getMethod(PersonRepository.class, methodName, argTypes);
		doReturn(Person.class).when(metadataMock).getReturnedDomainClass(method);
		doReturn(TypeInformation.of(Person.class)).when(metadataMock).getDomainTypeInformation();
		doReturn(TypeInformation.of(Person.class)).when(metadataMock).getReturnType(method);

		PartTree partTree = new PartTree(method.getName(), method.getReturnType());
		QUERY_CREATOR creator = queryCreator(partTree, new ParametersParameterAccessor(
				new QueryMethod(method, metadataMock, new SpelAwareProxyProjectionFactory()).getParameters(), args));

		KeyValueQuery<CRITERIA> q = creator.createQuery();
		return finalizeQuery(q, args);
	}

	private Method getMethod(Class<?> type, String methodName, Class<?>[] argTypes) throws NoSuchMethodException {

		for (Method declaredMethod : type.getDeclaredMethods()) {

			if (!declaredMethod.getName().equals(methodName)) {
				continue;
			}

			if (declaredMethod.getParameterCount() != argTypes.length) {
				continue;
			}

			Class<?>[] types = declaredMethod.getParameterTypes();

			boolean assigable = true;
			for (int i = 0; i < types.length; i++) {

				if (!types[i].isAssignableFrom(argTypes[i])) {
					assigable = false;
					break;
				}
			}

			if (assigable) {
				return declaredMethod;
			}
		}

		throw new NoSuchMethodException("Method " + methodName + " not found in " + type);
	}

	protected abstract QUERY_CREATOR queryCreator(PartTree partTree, ParametersParameterAccessor accessor);

	protected abstract KeyValueQuery<CRITERIA> finalizeQuery(KeyValueQuery<CRITERIA> query, Object... args);

	interface PersonRepository extends CrudRepository<Person, String> {

		// No arguments
		Person findBy();

		// Type.SIMPLE_PROPERTY
		Person findByFirstname(String firstname);

		// Type.NEGATING_SIMPLE_PROPERTY
		Person findByFirstnameNot(String firstname);

		// Type.TRUE
		Person findBySkinChangerIsTrue();

		// Type.FALSE
		Person findBySkinChangerIsFalse();

		// Type.IS_NULL
		Person findByLastnameIsNull();

		// Type.IS_NOT_NULL
		Person findByLastnameIsNotNull();

		// Type.STARTING_WITH
		Person findByFirstnameStartingWith(String firstanme);

		Person findByFirstnameIgnoreCase(String firstanme);

		// Type.AFTER
		Person findByBirthdayAfter(Date date);

		// Type.GREATHER_THAN
		Person findByAgeGreaterThan(Integer age);

		// Type.GREATER_THAN_EQUAL
		Person findByAgeGreaterThanEqual(Integer age);

		// Type.BEFORE
		Person findByBirthdayBefore(Date date);

		// Type.LESS_THAN
		Person findByAgeLessThan(Integer age);

		// Type.LESS_THAN_EQUAL
		Person findByAgeLessThanEqual(Integer age);

		// Type.BETWEEN
		Person findByAgeBetween(Integer low, Integer high);

		// Type.LIKE
		Person findByFirstnameLike(String firstname);

		// Type.NOT_LIKE
		Person findByFirstnameNotLike(String firstname);

		// Type.ENDING_WITH
		Person findByFirstnameEndingWith(String firstname);

		Person findByAgeGreaterThanAndLastname(Integer age, String lastname);

		Person findByAgeGreaterThanOrLastname(Integer age, String lastname);

		// Type.REGEX
		Person findByLastnameMatches(String lastname);

		// Type.IN
		Person findByFirstnameIn(List<String> in);

		// Type.NOT_IN
		Person findByFirstnameNotIn(List<String> in);

	}

	public interface Evaluation {
		Boolean against(Object candidate);

		boolean evaluate();
	}

	public static class Person {

		private @Id String id;
		private String firstname, lastname;
		private int age;
		private boolean isSkinChanger = false;
		private Date birthday;

		public Person() {}

		Person(String firstname, int age) {
			super();
			this.firstname = firstname;
			this.age = age;
		}

		Person skinChanger(boolean isSkinChanger) {
			this.isSkinChanger = isSkinChanger;
			return this;
		}

		Person named(String lastname) {
			this.lastname = lastname;
			return this;
		}

		Person bornAt(Date date) {
			this.birthday = date;
			return this;
		}

		public String getId() {
			return this.id;
		}

		public String getFirstname() {
			return this.firstname;
		}

		public String getLastname() {
			return this.lastname;
		}

		public int getAge() {
			return this.age;
		}

		public boolean isSkinChanger() {
			return this.isSkinChanger;
		}

		public Date getBirthday() {
			return this.birthday;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public void setSkinChanger(boolean isSkinChanger) {
			this.isSkinChanger = isSkinChanger;
		}

		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}
	}
}
