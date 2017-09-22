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
package org.springframework.data.keyvalue.repository.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.querydsl.QSort;
import org.springframework.util.Assert;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.OrderSpecifier.NullHandling;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * Utilities for Querydsl usage.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 */
abstract class KeyValueQuerydslUtils {

	private KeyValueQuerydslUtils() {
		// prevent instantiation
	}

	/**
	 * Transforms a plain {@link Order} into a QueryDsl specific {@link OrderSpecifier}.
	 *
	 * @param sort must not be {@literal null}.
	 * @param builder must not be {@literal null}.
	 * @return empty {@code OrderSpecifier<?>[]} when sort is {@literal null}.
	 */
	static OrderSpecifier<?>[] toOrderSpecifier(Sort sort, PathBuilder<?> builder) {

		Assert.notNull(sort, "Sort must not be null.");
		Assert.notNull(builder, "Builder must not be null.");

		List<OrderSpecifier<?>> specifiers = null;

		if (sort instanceof QSort) {
			specifiers = ((QSort) sort).getOrderSpecifiers();
		} else {

			specifiers = new ArrayList<>();
			for (Order order : sort) {
				specifiers.add(toOrderSpecifier(order, builder));
			}
		}

		return specifiers.toArray(new OrderSpecifier<?>[specifiers.size()]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static OrderSpecifier<?> toOrderSpecifier(Order order, PathBuilder<?> builder) {
		return new OrderSpecifier(
				order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
				buildOrderPropertyPathFrom(order, builder), toQueryDslNullHandling(order.getNullHandling()));
	}

	/**
	 * Creates an {@link Expression} for the given {@link Order} property.
	 *
	 * @param order must not be {@literal null}.
	 * @param builder must not be {@literal null}.
	 * @return
	 */
	private static Expression<?> buildOrderPropertyPathFrom(Order order, PathBuilder<?> builder) {

		Assert.notNull(order, "Order must not be null!");
		Assert.notNull(builder, "Builder must not be null!");

		PropertyPath path = PropertyPath.from(order.getProperty(), builder.getType());
		Expression<?> sortPropertyExpression = builder;

		while (path != null) {

			if (!path.hasNext() && order.isIgnoreCase()) {
				// if order is ignore-case we have to treat the last path segment as a String.
				sortPropertyExpression = Expressions.stringPath((Path<?>) sortPropertyExpression, path.getSegment()).lower();
			} else {
				sortPropertyExpression = Expressions.path(path.getType(), (Path<?>) sortPropertyExpression, path.getSegment());
			}

			path = path.next();
		}

		return sortPropertyExpression;
	}

	/**
	 * Converts the given {@link org.springframework.data.domain.Sort.NullHandling} to the appropriate Querydsl
	 * {@link NullHandling}.
	 *
	 * @param nullHandling must not be {@literal null}.
	 * @return
	 */
	private static NullHandling toQueryDslNullHandling(org.springframework.data.domain.Sort.NullHandling nullHandling) {

		Assert.notNull(nullHandling, "NullHandling must not be null!");

		switch (nullHandling) {

			case NULLS_FIRST:
				return NullHandling.NullsFirst;

			case NULLS_LAST:
				return NullHandling.NullsLast;

			case NATIVE:
			default:
				return NullHandling.Default;
		}
	}
}
