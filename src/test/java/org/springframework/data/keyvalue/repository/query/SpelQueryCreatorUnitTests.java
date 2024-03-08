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
package org.springframework.data.keyvalue.repository.query;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class SpelQueryCreatorUnitTests extends AbstractQueryCreatorTestBase<SpelQueryCreator, SpelExpression> {

	@Override
	protected SpelQueryCreator queryCreator(PartTree partTree, ParametersParameterAccessor accessor) {
		return new SpelQueryCreator(partTree, accessor);
	}

	@Override
	protected KeyValueQuery<SpelExpression> finalizeQuery(KeyValueQuery<SpelExpression> query, Object... args) {

		query.getCriteria().setEvaluationContext(
				SimpleEvaluationContext.forReadOnlyDataBinding().withRootObject(args).withInstanceMethods().build());
		return query;
	}

	@Override
	protected Evaluation createEvaluation(SpelExpression spelExpression) {
		return new SpelEvaluation(spelExpression);
	}

	static class SpelEvaluation implements Evaluation {

		SpelExpression expression;
		Object candidate;

		SpelEvaluation(SpelExpression expression) {
			this.expression = expression;
		}

		public Boolean against(Object candidate) {
			this.candidate = candidate;
			return evaluate();
		}

		public boolean evaluate() {
			expression.getEvaluationContext().setVariable("it", candidate);
			return expression.getValue(Boolean.class);
		}
	}
}
