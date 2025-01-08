/*
 * Copyright 2024-2025 the original author or authors.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.SimplePropertyPathAccessor;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * {@link AbstractQueryCreator} to create {@link Predicate}-based {@link KeyValueQuery}s.
 *
 * @author Christoph Strobl
 * @since 3.3
 */
public class PredicateQueryCreator extends AbstractQueryCreator<KeyValueQuery<Predicate<?>>, Predicate<?>> {

	public PredicateQueryCreator(PartTree tree, ParameterAccessor parameters) {
		super(tree, parameters);
	}

	@Override
	protected Predicate<?> create(Part part, Iterator<Object> iterator) {

		switch (part.getType()) {
			case TRUE:
				return PredicateBuilder.propertyValueOf(part).isTrue();
			case FALSE:
				return PredicateBuilder.propertyValueOf(part).isFalse();
			case SIMPLE_PROPERTY:
				return PredicateBuilder.propertyValueOf(part).isEqualTo(iterator.next());
			case IS_NULL:
				return PredicateBuilder.propertyValueOf(part).isNull();
			case IS_NOT_NULL:
				return PredicateBuilder.propertyValueOf(part).isNotNull();
			case LIKE:
				return PredicateBuilder.propertyValueOf(part).contains(iterator.next());
			case STARTING_WITH:
				return PredicateBuilder.propertyValueOf(part).startsWith(iterator.next());
			case AFTER:
			case GREATER_THAN:
				return PredicateBuilder.propertyValueOf(part).isGreaterThan(iterator.next());
			case GREATER_THAN_EQUAL:
				return PredicateBuilder.propertyValueOf(part).isGreaterThanEqual(iterator.next());
			case BEFORE:
			case LESS_THAN:
				return PredicateBuilder.propertyValueOf(part).isLessThan(iterator.next());
			case LESS_THAN_EQUAL:
				return PredicateBuilder.propertyValueOf(part).isLessThanEqual(iterator.next());
			case ENDING_WITH:
				return PredicateBuilder.propertyValueOf(part).endsWith(iterator.next());
			case BETWEEN:
				return PredicateBuilder.propertyValueOf(part).isGreaterThan(iterator.next())
						.and(PredicateBuilder.propertyValueOf(part).isLessThan(iterator.next()));
			case REGEX:
				return PredicateBuilder.propertyValueOf(part).matches(iterator.next());
			case IN:
				return PredicateBuilder.propertyValueOf(part).in(iterator.next());
			default:
				throw new InvalidDataAccessApiUsageException(String.format("Found invalid part '%s' in query", part.getType()));

		}
	}

	@Override
	protected Predicate<?> and(Part part, Predicate<?> base, Iterator<Object> iterator) {
		return base.and((Predicate) create(part, iterator));
	}

	@Override
	protected Predicate<?> or(Predicate<?> base, Predicate<?> criteria) {
		return base.or((Predicate) criteria);
	}

	@Override
	protected KeyValueQuery<Predicate<?>> complete(@Nullable Predicate<?> criteria, Sort sort) {
		if (criteria == null) {
			return new KeyValueQuery<>(it -> true, sort);
		}
		return new KeyValueQuery<>(criteria, sort);
	}

	static class PredicateBuilder {

		private final Part part;

		public PredicateBuilder(Part part) {
			this.part = part;
		}

		static PredicateBuilder propertyValueOf(Part part) {
			return new PredicateBuilder(part);
		}

		public Predicate<Object> isTrue() {
			return new ValueComparingPredicate(part.getProperty(), true);
		}

		public Predicate<Object> isFalse() {
			return new ValueComparingPredicate(part.getProperty(), false);
		}

		public Predicate<Object> isEqualTo(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (!ObjectUtils.nullSafeEquals(IgnoreCaseType.NEVER, part.shouldIgnoreCase())) {
					if (o instanceof String s1 && value instanceof String s2) {
						return s1.equalsIgnoreCase(s2);
					}
				}
				return ObjectUtils.nullSafeEquals(o, value);

			});
		}

		public Predicate<Object> isNull() {
			return new ValueComparingPredicate(part.getProperty(), Objects::isNull);
		}

		public Predicate<Object> isNotNull() {
			return isNull().negate();
		}

		public Predicate<Object> isLessThan(Object value) {
			return new ValueComparingPredicate(part.getProperty(),
					o -> NullSafeComparator.NULLS_HIGH.compare(o, value) == -1 ? true : false);
		}

		public Predicate<Object> isLessThanEqual(Object value) {
			return new ValueComparingPredicate(part.getProperty(),
					o -> NullSafeComparator.NULLS_HIGH.compare(o, value) <= 0 ? true : false);
		}

		public Predicate<Object> isGreaterThan(Object value) {
			return new ValueComparingPredicate(part.getProperty(),
					o -> NullSafeComparator.NULLS_HIGH.compare(o, value) == 1 ? true : false);
		}

		public Predicate<Object> isGreaterThanEqual(Object value) {
			return new ValueComparingPredicate(part.getProperty(),
					o -> NullSafeComparator.NULLS_HIGH.compare(o, value) >= 0 ? true : false);
		}

		public Predicate<Object> matches(Pattern pattern) {

			return new ValueComparingPredicate(part.getProperty(), o -> {
				if (o == null) {
					return false;
				}

				return pattern.matcher(o.toString()).find();
			});
		}

		public Predicate<Object> matches(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (o == null || value == null) {
					return ObjectUtils.nullSafeEquals(o, value);
				}

				if (value instanceof Pattern pattern) {
					return pattern.matcher(o.toString()).find();
				}

				return o.toString().matches(value.toString());

			});
		}

		public Predicate<Object> matches(String regex) {
			return matches(Pattern.compile(regex));
		}

		public Predicate<Object> in(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (value instanceof Collection<?> collection) {

					if (o instanceof Collection<?> subSet) {
						collection.containsAll(subSet);
					}
					return collection.contains(o);
				}
				if (ObjectUtils.isArray(value)) {
					return ObjectUtils.containsElement(ObjectUtils.toObjectArray(value), value);
				}
				return false;

			});
		}

		public Predicate<Object> contains(Object value) {

			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (o == null) {
					return false;
				}

				if (o instanceof Collection<?> collection) {
					return collection.contains(value);
				}

				if (ObjectUtils.isArray(o)) {
					return ObjectUtils.containsElement(ObjectUtils.toObjectArray(o), value);
				}

				if (o instanceof Map<?, ?> map) {
					return map.values().contains(value);
				}

				if (value == null) {
					return false;
				}

				String s = o.toString();

				if (ObjectUtils.nullSafeEquals(IgnoreCaseType.NEVER, part.shouldIgnoreCase())) {
					return s.contains(value.toString());
				}
				return s.toLowerCase().contains(value.toString().toLowerCase());

			});
		}

		public Predicate<Object> startsWith(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (!(o instanceof String s)) {
					return false;
				}

				if (ObjectUtils.nullSafeEquals(IgnoreCaseType.NEVER, part.shouldIgnoreCase())) {
					return s.startsWith(value.toString());
				}

				return s.toLowerCase().startsWith(value.toString().toLowerCase());
			});

		}

		public Predicate<Object> endsWith(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (!(o instanceof String s)) {
					return false;
				}

				if (ObjectUtils.nullSafeEquals(IgnoreCaseType.NEVER, part.shouldIgnoreCase())) {
					return s.endsWith(value.toString());
				}

				return s.toLowerCase().endsWith(value.toString().toLowerCase());
			});

		}
	}

	static class ValueComparingPredicate implements Predicate<Object> {

		private final PropertyPath path;
		private final Function<Object, Boolean> check;

		public ValueComparingPredicate(PropertyPath path, Object expected) {
			this(path, (value) -> ObjectUtils.nullSafeEquals(value, expected));
		}

		public ValueComparingPredicate(PropertyPath path, Function<Object, Boolean> check) {
			this.path = path;
			this.check = check;
		}

		@Override
		public boolean test(Object o) {
			Object value = new SimplePropertyPathAccessor<>(o).getValue(path);
			return check.apply(value);
		}
	}

}
