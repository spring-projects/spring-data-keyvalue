/*
 * Copyright 2016-2022 the original author or authors.
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

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.util.Assert;

/**
 * {@link SpelCriteria} allows to pass on a {@link SpelExpression} and {@link EvaluationContext} to the actual query
 * processor. This decouples the {@link SpelExpression} from the context it is used in.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class SpelCriteria {

	private final SpelExpression expression;
	private final EvaluationContext context;

	/**
	 * Creates a new {@link SpelCriteria} for the given {@link SpelExpression}.
	 *
	 * @param expression must not be {@literal null}.
	 */
	public SpelCriteria(SpelExpression expression) {
		this(expression, SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build());
	}

	/**
	 * Creates new {@link SpelCriteria}.
	 *
	 * @param expression must not be {@literal null}.
	 * @param context must not be {@literal null}.
	 */
	public SpelCriteria(SpelExpression expression, EvaluationContext context) {

		Assert.notNull(expression, "SpEL expression must not be null!");
		Assert.notNull(context, "EvaluationContext must not be null!");

		this.expression = expression;
		this.context = context;
	}

	/**
	 * @return will never be {@literal null}.
	 */
	public EvaluationContext getContext() {
		return context;
	}

	/**
	 * @return will never be {@literal null}.
	 */
	public SpelExpression getExpression() {
		return expression;
	}
}
