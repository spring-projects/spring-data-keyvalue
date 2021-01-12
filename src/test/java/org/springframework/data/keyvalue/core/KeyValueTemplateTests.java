/*
 * Copyright 2014-2021 the original author or authors.
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
package org.springframework.data.keyvalue.core;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.map.MapKeyValueAdapter;

/**
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueTemplateTests {

	static final Foo FOO_ONE = new Foo("one");
	static final Foo FOO_TWO = new Foo("two");
	static final Foo FOO_THREE = new Foo("three");
	static final Bar BAR_ONE = new Bar("one");
	static final ClassWithTypeAlias ALIASED = new ClassWithTypeAlias("super");
	static final SubclassOfAliasedType SUBCLASS_OF_ALIASED = new SubclassOfAliasedType("sub");
	static final KeyValueQuery<String> STRING_QUERY = new KeyValueQuery<>("foo == 'two'");

	KeyValueTemplate operations;

	@Before
	public void setUp() throws InstantiationException, IllegalAccessException {
		this.operations = new KeyValueTemplate(new MapKeyValueAdapter());
	}

	@After
	public void tearDown() throws Exception {
		this.operations.destroy();
	}

	@Test // DATACMNS-525
	public void insertShouldNotThorwErrorWhenExecutedHavingNonExistingIdAndNonNullValue() {
		operations.insert("1", FOO_ONE);
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExceptionForNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.insert(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExceptionForNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.insert("some-id", null));
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExecptionWhenObjectOfSameTypeAlreadyExists() {

		operations.insert("1", FOO_ONE);

		assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> operations.insert("1", FOO_TWO));
	}

	@Test // DATACMNS-525
	public void insertShouldWorkCorrectlyWhenObjectsOfDifferentTypesWithSameIdAreInserted() {

		operations.insert("1", FOO_ONE);
		operations.insert("1", BAR_ONE);
	}

	@Test // DATACMNS-525
	public void createShouldReturnSameInstanceGenerateId() {

		ClassWithStringId source = new ClassWithStringId();
		ClassWithStringId target = operations.insert(source);

		assertThat(target).isSameAs(source);
	}

	@Test // DATACMNS-525
	public void createShouldRespectExistingId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "one";

		operations.insert(source);

		assertThat(operations.findById("one", ClassWithStringId.class)).isEqualTo(Optional.of(source));
	}

	@Test // DATACMNS-525
	public void findByIdShouldReturnObjectWithMatchingIdAndType() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("1", Foo.class)).isEqualTo(Optional.of(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void findByIdSouldReturnOptionalEmptyIfNoMatchingIdFound() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("2", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	public void findByIdShouldReturnOptionalEmptyIfNoMatchingTypeFound() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("1", Bar.class)).isEmpty();
	}

	@Test // DATACMNS-525
	public void findShouldExecuteQueryCorrectly() {

		operations.insert("1", FOO_ONE);
		operations.insert("2", FOO_TWO);

		List<Foo> result = (List<Foo>) operations.find(STRING_QUERY, Foo.class);
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(FOO_TWO);
	}

	@Test // DATACMNS-525
	public void readShouldReturnEmptyCollectionIfOffsetOutOfRange() {

		operations.insert("1", FOO_ONE);
		operations.insert("2", FOO_TWO);
		operations.insert("3", FOO_THREE);

		assertThat(operations.findInRange(5, 5, Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	public void updateShouldReplaceExistingObject() {

		operations.insert("1", FOO_ONE);
		operations.update("1", FOO_TWO);
		assertThat(operations.findById("1", Foo.class)).isEqualTo(Optional.of(FOO_TWO));
	}

	@Test // DATACMNS-525
	public void updateShouldRespectTypeInformation() {

		operations.insert("1", FOO_ONE);
		operations.update("1", BAR_ONE);

		assertThat(operations.findById("1", Foo.class)).isEqualTo(Optional.of(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void deleteShouldRemoveObjectCorrectly() {

		operations.insert("1", FOO_ONE);
		operations.delete("1", Foo.class);
		assertThat(operations.findById("1", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	public void deleteReturnsNullWhenNotExisting() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.delete("2", Foo.class)).isNull();
	}

	@Test // DATACMNS-525
	public void deleteReturnsRemovedObject() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.delete("1", Foo.class)).isEqualTo(FOO_ONE);
	}

	@Test // DATACMNS-525
	public void deleteThrowsExceptionWhenIdCannotBeExctracted() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.delete(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void countShouldReturnZeroWhenNoElementsPresent() {
		assertThat(operations.count(Foo.class)).isEqualTo(0L);
	}

	@Test // DATACMNS-525
	public void insertShouldRespectTypeAlias() {

		operations.insert("1", ALIASED);
		operations.insert("2", SUBCLASS_OF_ALIASED);

		assertThat((List) operations.findAll(ALIASED.getClass())).contains(ALIASED, SUBCLASS_OF_ALIASED);
	}

	@Data
	@AllArgsConstructor
	static class Foo {

		String foo;

	}

	@Data
	@AllArgsConstructor
	static class Bar {

		String bar;
	}

	@Data
	static class ClassWithStringId implements Serializable {

		private static final long serialVersionUID = -7481030649267602830L;
		@Id String id;
		String value;
	}

	@ExplicitKeySpace(name = "aliased")
	@Data
	static class ClassWithTypeAlias implements Serializable {

		private static final long serialVersionUID = -5921943364908784571L;
		@Id String id;
		String name;

		public ClassWithTypeAlias(String name) {
			this.name = name;
		}
	}

	static class SubclassOfAliasedType extends ClassWithTypeAlias {

		private static final long serialVersionUID = -468809596668871479L;

		public SubclassOfAliasedType(String name) {
			super(name);
		}

	}

	@KeySpace
	@Persistent
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@interface ExplicitKeySpace {

		@AliasFor(annotation = KeySpace.class, value = "value")
		String name() default "";

	}
}
