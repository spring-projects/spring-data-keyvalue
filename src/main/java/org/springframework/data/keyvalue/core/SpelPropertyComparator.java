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

import java.util.Comparator;

import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link Comparator} implementation using {@link SpelExpression}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @param <T>
 */
public class SpelPropertyComparator<T> implements Comparator<T> {

	private static final Comparator<?> NULLS_FIRST = Comparator.nullsFirst(Comparator.naturalOrder());
	private static final Comparator<?> NULLS_LAST = Comparator.nullsLast(Comparator.naturalOrder());

	private final String path;
	private final SpelExpressionParser parser;

	private boolean asc = true;
	private boolean nullsFirst = true;
	private @Nullable SpelExpression expression;

	/**
	 * Create new {@link SpelPropertyComparator} for the given property path an {@link SpelExpressionParser}.
	 *
	 * @param path must not be {@literal null} or empty.
	 * @param parser must not be {@literal null}.
	 */
	public SpelPropertyComparator(String path, SpelExpressionParser parser) {

		Assert.hasText(path, "Path must not be null or empty");
		Assert.notNull(parser, "SpelExpressionParser must not be null");

		this.path = path;
		this.parser = parser;
	}

	/**
	 * Sort {@literal ascending}.
	 *
	 * @return
	 */
	public SpelPropertyComparator<T> asc() {
		this.asc = true;
		return this;
	}

	/**
	 * Sort {@literal descending}.
	 *
	 * @return
	 */
	public SpelPropertyComparator<T> desc() {
		this.asc = false;
		return this;
	}

	/**
	 * Sort {@literal null} values first.
	 *
	 * @return
	 */
	public SpelPropertyComparator<T> nullsFirst() {
		this.nullsFirst = true;
		return this;
	}

	/**
	 * Sort {@literal null} values last.
	 *
	 * @return
	 */
	public SpelPropertyComparator<T> nullsLast() {
		this.nullsFirst = false;
		return this;
	}

	/**
	 * Parse values to {@link SpelExpression}
	 *
	 * @return
	 */
	protected SpelExpression getExpression() {

		if (this.expression == null) {
			this.expression = parser.parseRaw(buildExpressionForPath());
		}

		return this.expression;
	}

	/**
	 * Create the expression raw value.
	 *
	 * @return
	 */
	protected String buildExpressionForPath() {

		String rawExpression = String.format("#comparator.compare(#arg1?.%s,#arg2?.%s)", path.replace(".", "?."),
				path.replace(".", "?."));

		return rawExpression;
	}

	@Override
	public int compare(T arg1, T arg2) {

		SpelExpression expressionToUse = getExpression();

		SimpleEvaluationContext ctx = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
		ctx.setVariable("comparator", nullsFirst ? NULLS_FIRST : NULLS_LAST);
		ctx.setVariable("arg1", arg1);
		ctx.setVariable("arg2", arg2);

		expressionToUse.setEvaluationContext(ctx);

		return expressionToUse.getValue(Integer.class) * (asc ? 1 : -1);
	}

	/**
	 * Get dot path to property.
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}
}
