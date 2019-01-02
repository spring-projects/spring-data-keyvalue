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
package org.springframework.data.keyvalue.core;

import static java.lang.Integer.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.*;

import java.util.Comparator;

import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Unit tests for {@link SpelPropertyComparator}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class SpelPropertyComperatorUnitTests {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private static final SomeType ONE = new SomeType("one", 1, 1);
	private static final SomeType TWO = new SomeType("two", 2, 2);
	private static final WrapperType WRAPPER_ONE = new WrapperType("w-one", ONE);
	private static final WrapperType WRAPPER_TWO = new WrapperType("w-two", TWO);

	@Test // DATACMNS-525
	public void shouldCompareStringAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("stringProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO), is(ONE.getStringProperty().compareTo(TWO.getStringProperty())));
	}

	@Test // DATACMNS-525
	public void shouldCompareStringDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO), is(TWO.getStringProperty().compareTo(ONE.getStringProperty())));
	}

	@Test // DATACMNS-525
	public void shouldCompareIntegerAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("integerProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO), is(ONE.getIntegerProperty().compareTo(TWO.getIntegerProperty())));
	}

	@Test // DATACMNS-525
	public void shouldCompareIntegerDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("integerProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO), is(TWO.getIntegerProperty().compareTo(ONE.getIntegerProperty())));
	}

	@Test // DATACMNS-525
	public void shouldComparePrimitiveIntegerAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("primitiveProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO),
				is(valueOf(ONE.getPrimitiveProperty()).compareTo(valueOf(TWO.getPrimitiveProperty()))));
	}

	@Test // DATACMNS-525
	public void shouldNotFailOnNullValues() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("stringProperty", PARSER);
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2)), is(1));
	}

	@Test // DATACMNS-525
	public void shouldComparePrimitiveIntegerDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("primitiveProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO),
				is(valueOf(TWO.getPrimitiveProperty()).compareTo(valueOf(ONE.getPrimitiveProperty()))));
	}

	@Test // DATACMNS-525
	public void shouldSortNullsFirstCorrectly() {
		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).nullsFirst();
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2)), equalTo(1));
	}

	@Test // DATACMNS-525
	public void shouldSortNullsLastCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).nullsLast();
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2)), equalTo(-1));
	}

	@Test // DATACMNS-525
	public void shouldCompareNestedTypesCorrectly() {

		Comparator<WrapperType> comparator = new SpelPropertyComparator<>("nestedType.stringProperty", PARSER);
		assertThat(comparator.compare(WRAPPER_ONE, WRAPPER_TWO),
				is(WRAPPER_ONE.getNestedType().getStringProperty().compareTo(WRAPPER_TWO.getNestedType().getStringProperty())));
	}

	@Test // DATACMNS-525
	public void shouldCompareNestedTypesCorrectlyWhenOneOfThemHasNullValue() {

		SpelPropertyComparator<WrapperType> comparator = new SpelPropertyComparator<>("nestedType.stringProperty", PARSER);
		assertThat(comparator.compare(WRAPPER_ONE, new WrapperType("two", null)), is(greaterThanOrEqualTo(1)));
	}

	static class WrapperType {

		private String stringPropertyWrapper;
		private SomeType nestedType;

		public WrapperType(String stringPropertyWrapper, SomeType nestedType) {
			this.stringPropertyWrapper = stringPropertyWrapper;
			this.nestedType = nestedType;
		}

		public String getStringPropertyWrapper() {
			return stringPropertyWrapper;
		}

		public void setStringPropertyWrapper(String stringPropertyWrapper) {
			this.stringPropertyWrapper = stringPropertyWrapper;
		}

		public SomeType getNestedType() {
			return nestedType;
		}

		public void setNestedType(SomeType nestedType) {
			this.nestedType = nestedType;
		}

	}

	static class SomeType {

		public SomeType() {

		}

		public SomeType(String stringProperty, Integer integerProperty, int primitiveProperty) {
			this.stringProperty = stringProperty;
			this.integerProperty = integerProperty;
			this.primitiveProperty = primitiveProperty;
		}

		String stringProperty;
		Integer integerProperty;
		int primitiveProperty;

		public String getStringProperty() {
			return stringProperty;
		}

		public void setStringProperty(String stringProperty) {
			this.stringProperty = stringProperty;
		}

		public Integer getIntegerProperty() {
			return integerProperty;
		}

		public void setIntegerProperty(Integer integerProperty) {
			this.integerProperty = integerProperty;
		}

		public int getPrimitiveProperty() {
			return primitiveProperty;
		}

		public void setPrimitiveProperty(int primitiveProperty) {
			this.primitiveProperty = primitiveProperty;
		}

	}

}
