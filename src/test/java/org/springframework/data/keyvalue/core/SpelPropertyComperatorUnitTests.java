/*
 * Copyright 2014-2024 the original author or authors.
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

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Unit tests for {@link SpelPropertyComparator}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
class SpelPropertyComperatorUnitTests {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private static final SomeType ONE = new SomeType("one", 1, 1);
	private static final SomeType TWO = new SomeType("two", 2, 2);
	private static final WrapperType WRAPPER_ONE = new WrapperType("w-one", ONE);
	private static final WrapperType WRAPPER_TWO = new WrapperType("w-two", TWO);

	@Test // DATACMNS-525
	void shouldCompareStringAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("stringProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO)).isEqualTo(ONE.getStringProperty().compareTo(TWO.getStringProperty()));
	}

	@Test // DATACMNS-525
	void shouldCompareStringDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO)).isEqualTo(TWO.getStringProperty().compareTo(ONE.getStringProperty()));
	}

	@Test // DATACMNS-525
	void shouldCompareIntegerAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("integerProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO)).isEqualTo(ONE.getIntegerProperty().compareTo(TWO.getIntegerProperty()));
	}

	@Test // DATACMNS-525
	void shouldCompareIntegerDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("integerProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO)).isEqualTo(TWO.getIntegerProperty().compareTo(ONE.getIntegerProperty()));
	}

	@Test // DATACMNS-525
	void shouldComparePrimitiveIntegerAscCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("primitiveProperty", PARSER);
		assertThat(comparator.compare(ONE, TWO))
				.isEqualTo(Integer.compare(ONE.getPrimitiveProperty(), TWO.getPrimitiveProperty()));
	}

	@Test // DATACMNS-525
	void shouldNotFailOnNullValues() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<>("stringProperty", PARSER);
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2))).isEqualTo(1);
	}

	@Test // DATACMNS-525
	void shouldComparePrimitiveIntegerDescCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("primitiveProperty", PARSER).desc();
		assertThat(comparator.compare(ONE, TWO))
				.isEqualTo(Integer.compare(TWO.getPrimitiveProperty(), ONE.getPrimitiveProperty()));
	}

	@Test // DATACMNS-525
	void shouldSortNullsFirstCorrectly() {
		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).nullsFirst();
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2))).isEqualTo(1);
	}

	@Test // DATACMNS-525
	void shouldSortNullsLastCorrectly() {

		Comparator<SomeType> comparator = new SpelPropertyComparator<SomeType>("stringProperty", PARSER).nullsLast();
		assertThat(comparator.compare(ONE, new SomeType(null, null, 2))).isEqualTo(-1);
	}

	@Test // DATACMNS-525
	void shouldCompareNestedTypesCorrectly() {

		Comparator<WrapperType> comparator = new SpelPropertyComparator<>("nestedType.stringProperty", PARSER);
		assertThat(comparator.compare(WRAPPER_ONE, WRAPPER_TWO)).isEqualTo(
				WRAPPER_ONE.getNestedType().getStringProperty().compareTo(WRAPPER_TWO.getNestedType().getStringProperty()));
	}

	@Test // DATACMNS-525
	void shouldCompareNestedTypesCorrectlyWhenOneOfThemHasNullValue() {

		SpelPropertyComparator<WrapperType> comparator = new SpelPropertyComparator<>("nestedType.stringProperty", PARSER);
		assertThat(comparator.compare(WRAPPER_ONE, new WrapperType("two", null))).isGreaterThanOrEqualTo(1);
	}

	public static class WrapperType {

		private String stringPropertyWrapper;
		private SomeType nestedType;

		WrapperType(String stringPropertyWrapper, SomeType nestedType) {
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

	@SuppressWarnings("WeakerAccess")
	public static class SomeType {

		public SomeType() {

		}

		SomeType(String stringProperty, Integer integerProperty, int primitiveProperty) {
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
