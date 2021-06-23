/*
 * Copyright 2015-2021 the original author or authors.
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

import org.springframework.core.env.PropertyResolver;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link KeyValuePersistentEntity} implementation that adds specific meta-data such as the {@literal keySpace}..
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Tim Sazon
 * @param <T>
 */
public class BasicKeyValuePersistentEntity<T, P extends KeyValuePersistentProperty<P>>
		extends BasicPersistentEntity<T, P> implements KeyValuePersistentEntity<T, P> {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private static final KeySpaceResolver DEFAULT_FALLBACK_RESOLVER = ClassNameKeySpaceResolver.INSTANCE;

	private final @Nullable Expression keyspaceExpression;
	private final @Nullable String keyspace;

	/**
	 * @param information must not be {@literal null}.
	 * @param fallbackKeySpaceResolver can be {@literal null}.
	 */
	public BasicKeyValuePersistentEntity(TypeInformation<T> information,
			@Nullable KeySpaceResolver fallbackKeySpaceResolver) {

		super(information);

		Class<T> type = information.getType();
		String keySpace = AnnotationBasedKeySpaceResolver.INSTANCE.resolveKeySpace(type);

		if (StringUtils.hasText(keySpace)) {

			this.keyspace = keySpace;
			this.keyspaceExpression = detectExpression(keySpace);
		} else {

			this.keyspace = resolveKeyspace(fallbackKeySpaceResolver, type);
			this.keyspaceExpression = null;
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

	@Nullable
	private static String resolveKeyspace(@Nullable KeySpaceResolver fallbackKeySpaceResolver, Class<?> type) {
		return (fallbackKeySpaceResolver == null ? DEFAULT_FALLBACK_RESOLVER : fallbackKeySpaceResolver)
				.resolveKeySpace(type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity#getKeySpace()
	 */
	@Override
	public String getKeySpace() {
		String keySpace = keyspaceExpression == null //
				? keyspace //
				: keyspaceExpression.getValue(getEvaluationContext(null), String.class);

		if (keySpace != null) {
			PropertyResolver propertyResolver = getPropertyResolver();
			if (propertyResolver != null) {
				return propertyResolver.resolvePlaceholders(keySpace);
			}
		}

		return keySpace;
	}
}
