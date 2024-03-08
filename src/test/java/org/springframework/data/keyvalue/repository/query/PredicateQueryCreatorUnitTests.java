/*
 * Copyright 2024 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * @author Christoph Strobl
 */
class PredicateQueryCreatorUnitTests extends AbstractQueryCreatorTestBase<PredicateQueryCreator, Predicate<?>> {

	@Override
	@Test // DATACMNS-525
	void startsWithIgnoreCaseReturnsTrueWhenMatching() {
		assertThat(evaluate("findByFirstnameIgnoreCase", "RobB").against(ROBB)).isTrue();
	}

	@Override
	protected PredicateQueryCreator queryCreator(PartTree partTree, ParametersParameterAccessor accessor) {
		return new PredicateQueryCreator(partTree, accessor);
	}

	@Override
	protected KeyValueQuery<Predicate<?>> finalizeQuery(KeyValueQuery<Predicate<?>> query, Object... args) {
		return query;
	}

	@Override
	protected Evaluation createEvaluation(Predicate<?> predicate) {
		return new PredicateEvaluation(predicate);
	}

	static class PredicateEvaluation implements Evaluation {

		private final Predicate expression;
		private Object candidate;

		PredicateEvaluation(Predicate<?> expression) {
			this.expression = expression;
		}

		public Boolean against(Object candidate) {
			this.candidate = candidate;
			return evaluate();
		}

		public boolean evaluate() {
			return expression.test(candidate);
		}
	}
}
