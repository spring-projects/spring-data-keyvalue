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
package org.springframework.data.keyvalue.core;

import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class KeyValueTemplateTests {

	private static final Foo FOO_ONE = new Foo("one");
	private static final Foo FOO_TWO = new Foo("two");
	private static final Foo FOO_THREE = new Foo("three");
	private static final Bar BAR_ONE = new Bar("one");
	private static final ClassWithTypeAlias ALIASED = new ClassWithTypeAlias("super");
	private static final SubclassOfAliasedType SUBCLASS_OF_ALIASED = new SubclassOfAliasedType("sub");

	private static final KeyValueQuery<Predicate<Foo>> STRING_QUERY = new KeyValueQuery<>((Predicate<Foo>) foo -> foo.getFoo().equals("two"));

	private KeyValueTemplate operations;

	@BeforeEach
	void setUp() {
		this.operations = new KeyValueTemplate(new MapKeyValueAdapter());
	}

	@AfterEach
	void tearDown() throws Exception {
		this.operations.destroy();
	}

	@Test // DATACMNS-525
	void insertShouldNotThorwErrorWhenExecutedHavingNonExistingIdAndNonNullValue() {
		operations.insert("1", FOO_ONE);
	}

	@Test // DATACMNS-525
	void insertShouldThrowExceptionForNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.insert(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	void insertShouldThrowExceptionForNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.insert("some-id", null));
	}

	@Test // DATACMNS-525
	void insertShouldThrowExecptionWhenObjectOfSameTypeAlreadyExists() {

		operations.insert("1", FOO_ONE);

		assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> operations.insert("1", FOO_TWO));
	}

	@Test // DATACMNS-525
	void insertShouldWorkCorrectlyWhenObjectsOfDifferentTypesWithSameIdAreInserted() {

		operations.insert("1", FOO_ONE);
		operations.insert("1", BAR_ONE);
	}

	@Test // DATACMNS-525
	void createShouldReturnSameInstanceGenerateId() {

		ClassWithStringId source = new ClassWithStringId();
		ClassWithStringId target = operations.insert(source);

		assertThat(target).isSameAs(source);
	}

	@Test // DATACMNS-525
	void createShouldRespectExistingId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "one";

		operations.insert(source);

		assertThat(operations.findById("one", ClassWithStringId.class)).contains(source);
	}

	@Test // DATACMNS-525
	void findByIdShouldReturnObjectWithMatchingIdAndType() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("1", Foo.class)).contains(FOO_ONE);
	}

	@Test // DATACMNS-525
	void findByIdSouldReturnOptionalEmptyIfNoMatchingIdFound() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("2", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	void findByIdShouldReturnOptionalEmptyIfNoMatchingTypeFound() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.findById("1", Bar.class)).isEmpty();
	}

	@Test // DATACMNS-525
	void findShouldExecuteQueryCorrectly() {

		operations.insert("1", FOO_ONE);
		operations.insert("2", FOO_TWO);

		List<Foo> result = (List<Foo>) operations.find(STRING_QUERY, Foo.class);
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(FOO_TWO);
	}

	@Test // DATACMNS-525
	void readShouldReturnEmptyCollectionIfOffsetOutOfRange() {

		operations.insert("1", FOO_ONE);
		operations.insert("2", FOO_TWO);
		operations.insert("3", FOO_THREE);

		assertThat(operations.findInRange(5, 5, Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	void updateShouldReplaceExistingObject() {

		operations.insert("1", FOO_ONE);
		operations.update("1", FOO_TWO);
		assertThat(operations.findById("1", Foo.class)).contains(FOO_TWO);
	}

	@Test // DATACMNS-525
	void updateShouldRespectTypeInformation() {

		operations.insert("1", FOO_ONE);
		operations.update("1", BAR_ONE);

		assertThat(operations.findById("1", Foo.class)).contains(FOO_ONE);
	}

	@Test // DATACMNS-525
	void deleteShouldRemoveObjectCorrectly() {

		operations.insert("1", FOO_ONE);
		operations.delete("1", Foo.class);
		assertThat(operations.findById("1", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	void deleteReturnsNullWhenNotExisting() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.delete("2", Foo.class)).isNull();
	}

	@Test // DATACMNS-525
	void deleteReturnsRemovedObject() {

		operations.insert("1", FOO_ONE);
		assertThat(operations.delete("1", Foo.class)).isEqualTo(FOO_ONE);
	}

	@Test // DATACMNS-525
	void deleteThrowsExceptionWhenIdCannotBeExctracted() {
		assertThatIllegalArgumentException().isThrownBy(() -> operations.delete(FOO_ONE));
	}

	@Test // DATACMNS-525
	void countShouldReturnZeroWhenNoElementsPresent() {
		assertThat(operations.count(Foo.class)).isEqualTo(0L);
	}

	@Test // DATACMNS-525
	void insertShouldRespectTypeAlias() {

		operations.insert("1", ALIASED);
		operations.insert("2", SUBCLASS_OF_ALIASED);

		assertThat((List) operations.findAll(ALIASED.getClass())).contains(ALIASED, SUBCLASS_OF_ALIASED);
	}

	static class Foo {

		String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		public String getFoo() {
			return this.foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}
	}

	static class Bar {

		String bar;

		public Bar(String bar) {
			this.bar = bar;
		}

		public String getBar() {
			return this.bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}
	}

	static class ClassWithStringId implements Serializable {

		private static final long serialVersionUID = -7481030649267602830L;
		@Id String id;
		String value;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@ExplicitKeySpace(name = "aliased")
	static class ClassWithTypeAlias implements Serializable {

		private static final long serialVersionUID = -5921943364908784571L;
		@Id String id;
		String name;

		ClassWithTypeAlias(String name) {
			this.name = name;
		}

		public String getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static class SubclassOfAliasedType extends ClassWithTypeAlias {

		private static final long serialVersionUID = -468809596668871479L;

		SubclassOfAliasedType(String name) {
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
