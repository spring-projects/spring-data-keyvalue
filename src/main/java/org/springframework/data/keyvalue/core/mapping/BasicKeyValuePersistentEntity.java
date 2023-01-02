/*
 * Copyright 2015-2023 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link KeyValuePersistentEntity} implementation that adds specific meta-data such as the {@literal keySpace}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @param <T>
 */
public class BasicKeyValuePersistentEntity<T, P extends KeyValuePersistentProperty<P>>
		extends BasicPersistentEntity<T, P> implements KeyValuePersistentEntity<T, P> {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private final @Nullable Expression keyspaceExpression;
	private final @Nullable String keyspace;

	/**
	 * @param information must not be {@literal null}.
	 * @since 3.1
	 */
	public BasicKeyValuePersistentEntity(TypeInformation<T> information) {
		this(information, (String) null);
	}

	/**
	 * @param information must not be {@literal null}.
	 * @param keySpaceResolver can be {@literal null}.
	 */
	public BasicKeyValuePersistentEntity(TypeInformation<T> information, @Nullable KeySpaceResolver keySpaceResolver) {
		this(information, keySpaceResolver != null ? keySpaceResolver.resolveKeySpace(information.getType()) : null);
	}

	private BasicKeyValuePersistentEntity(TypeInformation<T> information, @Nullable String keyspace) {

		super(information);

		if (StringUtils.hasText(keyspace)) {

			this.keyspace = keyspace;
			this.keyspaceExpression = null;
		} else {

			Class<T> type = information.getType();
			String detectedKeyspace = AnnotationBasedKeySpaceResolver.INSTANCE.resolveKeySpace(type);

			if (StringUtils.hasText(detectedKeyspace)) {

				this.keyspace = detectedKeyspace;
				this.keyspaceExpression = detectExpression(detectedKeyspace);
			} else {

				this.keyspace = ClassNameKeySpaceResolver.INSTANCE.resolveKeySpace(type);
				this.keyspaceExpression = null;
			}
		}
	}

	/**
	 * Returns a SpEL {@link Expression} if the given {@link String} is actually an expression that does not evaluate to a
	 * {@link LiteralExpression} (indicating that no subsequent evaluation is necessary).
	 *
	 * @param potentialExpression must not be {@literal null}
	 * @return the parsed {@link Expression} or {@literal null}.
	 */
	@Nullable
	private static Expression detectExpression(String potentialExpression) {

		Expression expression = PARSER.parseExpression(potentialExpression, ParserContext.TEMPLATE_EXPRESSION);
		return expression instanceof LiteralExpression ? null : expression;
	}

	@Override
	public String getKeySpace() {
		return keyspaceExpression == null //
				? keyspace //
				: keyspaceExpression.getValue(getEvaluationContext(null), String.class);
	}
}
