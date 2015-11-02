/*
 * Copyright 2016 the original author or authors.
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

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * {@link SpelCriteria} allows to pass on a {@link SpelExpression} and {@link EvaluationContext} to the actual query
 * processor. This decouples the {@link SpelExpression} from the context it is used in.
 * 
 * @author Christoph Strobl
 */
public class SpelCriteria {

	private final SpelExpression expression;
	private final EvaluationContext context;

	/**
	 * Creates new {@link SpelCriteria}.
	 * 
	 * @param expression must not be {@literal null}.
	 * @param context can be {@literal null} and will be defaulted to {@link StandardEvaluationContext}.
	 */
	public SpelCriteria(SpelExpression expression, EvaluationContext context) {

		this.expression = expression;
		this.context = context == null ? new StandardEvaluationContext() : context;
	}

	/**
	 * @return never {@literal null}.
	 */
	public EvaluationContext getContext() {
		return context;
	}

	/**
	 * @return never {@literal null}.
	 */
	public SpelExpression getExpression() {
		return expression;
	}

}
