/*
 * Copyright 2014-2017 the original author or authors.
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
package org.springframework.data.keyvalue.repository.query;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
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
	public void equalsReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void equalsReturnsFalseWhenNotMatching() throws Exception {
		assertThat(evaluate("findByFirstname", BRAN.firstname).against(RICKON), is(false));
	}

	@Test // DATACMNS-525
	public void isTrueAssertedPropertlyWhenTrue() throws Exception {
		assertThat(evaluate("findBySkinChangerIsTrue").against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void isTrueAssertedPropertlyWhenFalse() throws Exception {
		assertThat(evaluate("findBySkinChangerIsTrue").against(RICKON), is(false));
	}

	@Test // DATACMNS-525
	public void isFalseAssertedPropertlyWhenTrue() throws Exception {
		assertThat(evaluate("findBySkinChangerIsFalse").against(BRAN), is(false));
	}

	@Test // DATACMNS-525
	public void isFalseAssertedPropertlyWhenFalse() throws Exception {
		assertThat(evaluate("findBySkinChangerIsFalse").against(RICKON), is(true));
	}

	@Test // DATACMNS-525
	public void isNullAssertedPropertlyWhenAttributeIsNull() throws Exception {
		assertThat(evaluate("findByLastnameIsNull").against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void isNullAssertedPropertlyWhenAttributeIsNotNull() throws Exception {
		assertThat(evaluate("findByLastnameIsNull").against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void isNotNullFalseTrueWhenAttributeIsNull() throws Exception {
		assertThat(evaluate("findByLastnameIsNotNull").against(BRAN), is(false));
	}

	@Test // DATACMNS-525
	public void isNotNullReturnsTrueAttributeIsNotNull() throws Exception {
		assertThat(evaluate("findByLastnameIsNotNull").against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void startsWithReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void startsWithReturnsFalseWhenNotMatching() throws Exception {
		assertThat(evaluate("findByFirstnameStartingWith", "r").against(BRAN), is(false));
	}

	@Test // DATACMNS-525
	public void likeReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByFirstnameLike", "ob").against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void likeReturnsFalseWhenNotMatching() throws Exception {
		assertThat(evaluate("findByFirstnameLike", "ra").against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void endsWithReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByFirstnameEndingWith", "bb").against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void endsWithReturnsFalseWhenNotMatching() throws Exception {
		assertThat(evaluate("findByFirstnameEndingWith", "an").against(ROBB), is(false));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class) // DATACMNS-525
	public void startsWithIgnoreCaseReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByFirstnameIgnoreCase", "R").against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void greaterThanReturnsTrueForHigherValues() throws Exception {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void greaterThanReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByAgeGreaterThan", BRAN.age).against(RICKON), is(false));
	}

	@Test // DATACMNS-525
	public void afterReturnsTrueForHigherValues() throws Exception {
		assertThat(evaluate("findByBirthdayAfter", ROBB.birthday).against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void afterReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByBirthdayAfter", BRAN.birthday).against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void greaterThanEaualsReturnsTrueForHigherValues() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void greaterThanEqualsReturnsTrueForEqualValues() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void greaterThanEqualsReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanEqual", BRAN.age).against(RICKON), is(false));
	}

	@Test // DATACMNS-525
	public void lessThanReturnsTrueForHigherValues() throws Exception {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void lessThanReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByAgeLessThan", BRAN.age).against(RICKON), is(true));
	}

	@Test // DATACMNS-525
	public void beforeReturnsTrueForLowerValues() throws Exception {
		assertThat(evaluate("findByBirthdayBefore", BRAN.birthday).against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void beforeReturnsFalseForHigherValues() throws Exception {
		assertThat(evaluate("findByBirthdayBefore", ROBB.birthday).against(BRAN), is(false));
	}

	@Test // DATACMNS-525
	public void lessThanEaualsReturnsTrueForHigherValues() throws Exception {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void lessThanEaualsReturnsTrueForEqualValues() throws Exception {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(BRAN), is(true));
	}

	@Test // DATACMNS-525
	public void lessThanEqualsReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByAgeLessThanEqual", BRAN.age).against(RICKON), is(true));
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsTrueForValuesInBetween() throws Exception {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(ARYA), is(true));
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsFalseForHigherValues() throws Exception {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(JON), is(false));
	}

	@Test // DATACMNS-525
	public void betweenEqualsReturnsFalseForLowerValues() throws Exception {
		assertThat(evaluate("findByAgeBetween", BRAN.age, ROBB.age).against(RICKON), is(false));
	}

	@Test // DATACMNS-525
	public void connectByAndReturnsTrueWhenAllPropertiesMatching() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(JON), is(true));
	}

	@Test // DATACMNS-525
	public void connectByAndReturnsFalseWhenOnlyFewPropertiesMatch() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanAndLastname", BRAN.age, JON.lastname).against(ROBB), is(false));
	}

	@Test // DATACMNS-525
	public void connectByOrReturnsTrueWhenOnlyFewPropertiesMatch() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(ROBB), is(true));
	}

	@Test // DATACMNS-525
	public void connectByOrReturnsTrueWhenAllPropertiesMatch() throws Exception {
		assertThat(evaluate("findByAgeGreaterThanOrLastname", BRAN.age, JON.lastname).against(JON), is(true));
	}

	@Test // DATACMNS-525
	public void regexReturnsTrueWhenMatching() throws Exception {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(JON), is(true));
	}

	@Test // DATACMNS-525
	public void regexReturnsFalseWhenNotMatching() throws Exception {
		assertThat(evaluate("findByLastnameMatches", "^s.*w$").against(ROBB), is(false));
	}

	@Test // DATAKV-169
	public void inReturnsMatchCorrectly() throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(ROBB), is(true));
	}

	@Test // DATAKV-169
	public void inNotMatchingReturnsCorrectly() throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(JON), is(false));
	}

	@Test // DATAKV-169
	public void inWithNullCompareValuesCorrectly() throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		list.add(null);

		assertThat(evaluate("findByFirstnameIn", list).against(JON), is(false));
	}

	@Test // DATAKV-169
	public void inWithNullSourceValuesMatchesCorrectly() throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		list.add(ROBB.firstname);

		assertThat(evaluate("findByFirstnameIn", list).against(new Person(null, 10)), is(false));
	}

	@Test // DATAKV-169
	public void inMatchesNullValuesCorrectly() throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		list.add(null);

		assertThat(evaluate("findByFirstnameIn", list).against(new Person(null, 10)), is(true));
	}

	private Evaluation evaluate(String methodName, Object... args) throws Exception {
		return new Evaluation((SpelExpression) createQueryForMethodWithArgs(methodName, args).getCritieria());
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
		q.getCritieria().setEvaluationContext(new StandardEvaluationContext(args));

		return q;
	}

	static interface PersonRepository {

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

		public Evaluation(SpelExpression expresison) {
			this.expression = expresison;
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

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public Date getBirthday() {
			return birthday;
		}

		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}

		public boolean isSkinChanger() {
			return isSkinChanger;
		}

		public void setSkinChanger(boolean isSkinChanger) {
			this.isSkinChanger = isSkinChanger;
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
