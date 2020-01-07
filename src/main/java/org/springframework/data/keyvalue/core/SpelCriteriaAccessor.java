/*
 * Copyright 2014-2020 the original author or authors.
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

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

/**
 * {@link CriteriaAccessor} implementation capable of {@link SpelExpression}s.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
class SpelCriteriaAccessor implements CriteriaAccessor<SpelCriteria> {

	private final SpelExpressionParser parser;

	/**
	 * Creates a new {@link SpelCriteriaAccessor} using the given {@link SpelExpressionParser}.
	 *
	 * @param parser must not be {@literal null}.
	 */
	public SpelCriteriaAccessor(SpelExpressionParser parser) {

		Assert.notNull(parser, "SpelExpressionParser must not be null!");

		this.parser = parser;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.CriteriaAccessor#resolve(org.springframework.data.keyvalue.core.query.KeyValueQuery)
	 */
	@Override
	public SpelCriteria resolve(KeyValueQuery<?> query) {

		if (query.getCriteria() == null) {
			return null;
		}

		if (query.getCriteria() instanceof SpelExpression) {
			return new SpelCriteria((SpelExpression) query.getCriteria());
		}

		if (query.getCriteria() instanceof String) {
			return new SpelCriteria(parser.parseRaw((String) query.getCriteria()));
		}

		if (query.getCriteria() instanceof SpelCriteria) {
			return (SpelCriteria) query.getCriteria();
		}

		throw new IllegalArgumentException("Cannot create SpelCriteria for " + query.getCriteria());
	}
}
