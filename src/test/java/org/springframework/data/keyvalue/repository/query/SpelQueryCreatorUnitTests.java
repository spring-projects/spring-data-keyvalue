/*
 * Copyright 2014-2019 the original author or authors.
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

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class SpelQueryCreatorUnitTests {

	static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

	static final Person RICKON = new Person("rickon", 4);
	static final Person BRAN = new Person("bran", 9)//
			.skinChanger(true)//
			.bornAt(FORMATTER.parseDateTime("2013-01-31T06:00:00Z").toDate());
	static final Person ARYA = new Person("arya", 13);
	static final Person ROBB = new Person("robb", 16)//
			.named("stark")//
			.bornAt(FORMATTER.parseDateTime("2010-09-20T06:00:00Z").toDate());
	static final Person JON = new Person("jon", 17).named("snow");

	@Mock RepositoryMetadata metadataMock;

	@Test // DATACMNS-525
	public void equalsReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void equalsReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	public void isTrueAssertedProperlyWhenTrue() {
		assertThat(evaluate("findBySkinChangerIsTrue").against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void isTrueAssertedProperlyWhenFalse() {
		assertThat(evaluate("findBySkinChangerIsTrue").against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	public void isFalseAssertedProperlyWhenTrue() {
		assertThat(evaluate("findBySkinChangerIsFalse").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	public void isFalseAssertedProperlyWhenFalse() {
		assertThat(evaluate("findBySkinChangerIsFalse").against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	public void isNullAssertedProperlyWhenAttributeIsNull() {
		assertThat(evaluate("findByLastnameIsNull").against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void isNullAssertedProperlyWhenAttributeIsNotNull() {
		assertThat(evaluate("findByLastnameIsNull").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void isNotNullFalseTrueWhenAttributeIsNull() {
		assertThat(evaluate("findByLastnameIsNotNull").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	public void isNotNullReturnsTrueAttributeIsNotNull() {
		assertThat(evaluate("findByLastnameIsNotNull").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void startsWithReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void startsWithReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	public void likeReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameLike", "ob").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void likeReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameLike", "ra").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void endsWithReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameEndingWith", "bb").against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void endsWithReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByFirstnameEndingWith", "an").against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void startsWithIgnoreCaseReturnsTrueWhenMatching() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
				.isThrownBy(() -> evaluate("findByFirstnameIgnoreCase", "R").against(ROBB));
	}

	@Test // DATACMNS-525
	public void greaterThanReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void greaterThanReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	public void afterReturnsTrueForHigherValues() {
		assertThat(evaluate("findByBirthdayAfter", ROBB.birthday).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void afterReturnsFalseForLowerValues() {
		assertThat(evaluate("findByBirthdayAfter", BRAN.birthday).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void greaterThanEaualsReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void greaterThanEqualsReturnsTrueForEqualValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void greaterThanEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	public void lessThanReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void lessThanReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	public void beforeReturnsTrueForLowerValues() {
		assertThat(evaluate("findByBirthdayBefore", BRAN.birthday).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void beforeReturnsFalseForHigherValues() {
		assertThat(evaluate("findByBirthdayBefore", ROBB.birthday).against(BRAN)).isFalse();
	}

	@Test // DATACMNS-525
	public void lessThanEaualsReturnsTrueForHigherValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void lessThanEaualsReturnsTrueForEqualValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(BRAN)).isTrue();
	}

	@Test // DATACMNS-525
	public void lessThanEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(RICKON)).isTrue();
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsTrueForValuesInBetween() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(ARYA)).isTrue();
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsFalseForHigherValues() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(JON)).isFalse();
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsFalseForLowerValues() {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(RICKON)).isFalse();
	}

	@Test // DATACMNS-525
	public void connectByAndReturnsTrueWhenAllPropertiesMatching() {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	public void connectByAndReturnsFalseWhenOnlyFewPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(ROBB)).isFalse();
	}

	@Test // DATACMNS-525
	public void connectByOrReturnsTrueWhenOnlyFewPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(ROBB)).isTrue();
	}

	@Test // DATACMNS-525
	public void connectByOrReturnsTrueWhenAllPropertiesMatch() {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	public void regexReturnsTrueWhenMatching() {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(JON)).isTrue();
	}

	@Test // DATACMNS-525
	public void regexReturnsFalseWhenNotMatching() {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(ROBB)).isFalse();
	}

	@Test // DATAKV-169
	public void inReturnsMatchCorrectly() {

		ArrayList<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(ROBB)).isTrue();
	}

	@Test // DATAKV-169
	public void inNotMatchingReturnsCorrectly() {

		ArrayList<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(JON)).isFalse();
	}

	@Test // DATAKV-169
	public void inWithNullCompareValuesCorrectly() {

		ArrayList<String> list = new ArrayList<>();
		list.add(null);

		assertThat(evaluate("findByFirstnameIn", list).against(JON)).isFalse();
	}

	@Test // DATAKV-169
	public void inWithNullSourceValuesMatchesCorrectly() {

		ArrayList<String> list = new ArrayList<>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(new Person(null, 10))).isFalse();
	}

	@Test // DATAKV-169
	public void inMatchesNullValuesCorrectly() {

		ArrayList<String> list = new ArrayList<>();
		list.add(null);

		assertThat(evaluate("findByFirstnameIn", list).against(new Person(null, 10))).isTrue();
	}

	@Test // DATAKV-185
	public void noDerivedQueryArgumentsMatchesAlways() {

		assertThat(evaluate("findBy").against(JON)).isTrue();
		assertThat(evaluate("findBy").against(null)).isTrue();
	}

	@SneakyThrows
	private Evaluation evaluate(String methodName, Object... args) {
		return new Evaluation(createQueryForMethodWithArgs(methodName, args).getCriteria());
	}

	private KeyValueQuery<SpelExpression> createQueryForMethodWithArgs(String methodName, Object... args)
			throws NoSuchMethodException, SecurityException {

		Class<?>[] argTypes = new Class<?>[args.length];
		if (!ObjectUtils.isEmpty(args)) {

			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
		}

		Method method = PersonRepository.class.getMethod(methodName, argTypes);
		doReturn(Person.class).when(metadataMock).getReturnedDomainClass(method);

		PartTree partTree = new PartTree(method.getName(), method.getReturnType());
		SpelQueryCreator creator = new SpelQueryCreator(partTree, new ParametersParameterAccessor(
				new QueryMethod(method, metadataMock, new SpelAwareProxyProjectionFactory()).getParameters(), args));

		KeyValueQuery<SpelExpression> q = creator.createQuery();
		q.getCriteria().setEvaluationContext(
				SimpleEvaluationContext.forReadOnlyDataBinding().withRootObject(args).withInstanceMethods().build());

		return q;
	}

	@SuppressWarnings("unused")
	interface PersonRepository {

		// No arguments
		Person findBy();

		// Type.SIMPLE_PROPERTY
		Person findByFirstname(String firstname);

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

		// Type.ENDING_WITH
		Person findByFirstnameEndingWith(String firstname);

		Person findByAgeGreaterThanAndLastname(Integer age, String lastname);

		Person findByAgeGreaterThanOrLastname(Integer age, String lastname);

		// Type.REGEX
		Person findByLastnameMatches(String lastname);

		// Type.IN
		Person findByFirstnameIn(ArrayList<String> in);

	}

	static class Evaluation {

		SpelExpression expression;
		Object candidate;

		public Evaluation(SpelExpression expression) {
			this.expression = expression;
		}

		public Boolean against(Object candidate) {
			this.candidate = candidate;
			return evaluate();
		}

		private boolean evaluate() {
			expression.getEvaluationContext().setVariable("it", candidate);
			return expression.getValue(Boolean.class);
		}
	}

	@Data
	static class Person {

		private @Id String id;
		private String firstname, lastname;
		private int age;
		private boolean isSkinChanger = false;
		private Date birthday;

		public Person() {}

		public Person(String firstname, int age) {
			super();
			this.firstname = firstname;
			this.age = age;
		}

		public Person skinChanger(boolean isSkinChanger) {
			this.isSkinChanger = isSkinChanger;
			return this;
		}

		public Person named(String lastname) {
			this.lastname = lastname;
			return this;
		}

		public Person bornAt(Date date) {
			this.birthday = date;
			return this;
		}
	}
}
