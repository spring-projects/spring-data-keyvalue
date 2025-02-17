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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
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
import org.springframework.lang.Contract;
import org.springframework.util.ObjectUtils;

/**
 * {@link AbstractQueryCreator} to create {@link Predicate}-based {@link KeyValueQuery}s.
 *
 * @author Christoph Strobl
 * @author Tom Van Wemmel
 * @author Mark Paluch
 * @since 3.3
 */
public class PredicateQueryCreator extends AbstractQueryCreator<KeyValueQuery<Predicate<?>>, Predicate<?>> {

	private static final Comparator<?> COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

	public PredicateQueryCreator(PartTree tree, ParameterAccessor parameters) {
		super(tree, parameters);
	}

	@Override
	protected Predicate<?> create(Part part, Iterator<Object> iterator) {

		PredicateBuilder builder = PredicateBuilder.propertyValueOf(part);

		return switch (part.getType()) {
			case TRUE -> builder.isTrue();
			case FALSE -> builder.isFalse();
			case SIMPLE_PROPERTY -> builder.isEqualTo(iterator.next());
			case NEGATING_SIMPLE_PROPERTY -> builder.isEqualTo(iterator.next()).negate();
			case IS_NULL -> builder.isNull();
			case IS_NOT_NULL -> builder.isNotNull();
			case LIKE -> builder.contains(iterator.next());
			case NOT_LIKE -> builder.contains(iterator.next()).negate();
			case STARTING_WITH -> builder.startsWith(iterator.next());
			case AFTER, GREATER_THAN -> builder.isGreaterThan(iterator.next());
			case GREATER_THAN_EQUAL -> builder.isGreaterThanEqual(iterator.next());
			case BEFORE, LESS_THAN -> builder.isLessThan(iterator.next());
			case LESS_THAN_EQUAL -> builder.isLessThanEqual(iterator.next());
			case ENDING_WITH -> builder.endsWith(iterator.next());
			case BETWEEN -> builder.isGreaterThan(iterator.next()).and(builder.isLessThan(iterator.next()));
			case REGEX -> builder.matches(iterator.next());
			case IN -> builder.in(iterator.next());
			case NOT_IN -> builder.in(iterator.next()).negate();
			default ->
				throw new InvalidDataAccessApiUsageException(String.format("Found invalid part '%s' in query", part.getType()));
		};
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Predicate<?> and(Part part, Predicate<?> base, Iterator<Object> iterator) {
		return base.and((Predicate) create(part, iterator));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Predicate<?> or(Predicate<?> base, Predicate<?> criteria) {
		return base.or((Predicate) criteria);
	}

	@Override
	protected KeyValueQuery<Predicate<?>> complete(@Nullable Predicate<?> criteria, Sort sort) {
		return criteria == null ? new KeyValueQuery<>(it -> true, sort) : new KeyValueQuery<>(criteria, sort);
	}

	static class PredicateBuilder {

		private final Part part;

		public PredicateBuilder(Part part) {
			this.part = part;
		}

		@SuppressWarnings("unchecked")
		static <T> Comparator<T> comparator() {
			return (Comparator<T>) COMPARATOR;
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

		@Contract("_ -> new")
		public Predicate<Object> isEqualTo(@Nullable Object value) {
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

		@Contract("_ -> new")
		public Predicate<Object> isLessThan(@Nullable Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> comparator().compare(o, value) < 0);
		}

		@Contract("_ -> new")
		public Predicate<Object> isLessThanEqual(@Nullable Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> comparator().compare(o, value) <= 0);
		}

		@Contract("_ -> new")
		public Predicate<Object> isGreaterThan(@Nullable Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> comparator().compare(o, value) > 0);
		}

		@Contract("_ -> new")
		public Predicate<Object> isGreaterThanEqual(@Nullable Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> comparator().compare(o, value) >= 0);
		}

		@Contract("!null -> new")
		public Predicate<Object> matches(Pattern pattern) {

			return new ValueComparingPredicate(part.getProperty(), o -> {
				if (o == null) {
					return false;
				}

				return pattern.matcher(o.toString()).find();
			});
		}

		@Contract("_ -> new")
		public Predicate<Object> matches(@Nullable Object value) {
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

		@Contract("!null -> new")
		public Predicate<Object> matches(String regex) {
			return matches(Pattern.compile(regex));
		}

		@Contract("!null -> new")
		public Predicate<Object> in(Object value) {
			return new ValueComparingPredicate(part.getProperty(), o -> {

				if (value instanceof Collection<?> collection) {

					if (o instanceof Collection<?> subSet) {
						return collection.containsAll(subSet);
					}

					return collection.contains(o);
				}
				if (ObjectUtils.isArray(value)) {
					return ObjectUtils.containsElement(ObjectUtils.toObjectArray(value), value);
				}
				return false;
			});
		}

		@Contract("_ -> new")
		public Predicate<Object> contains(@Nullable Object value) {

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
					return map.containsValue(value);
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

		@Contract("!null -> new")
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

		@Contract("!null -> new")
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
		private final Function<@Nullable Object, Boolean> check;

		public ValueComparingPredicate(PropertyPath path, @Nullable Object expected) {
			this(path, (value) -> ObjectUtils.nullSafeEquals(value, expected));
		}

		public ValueComparingPredicate(PropertyPath path, Function<@Nullable Object, Boolean> check) {
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
