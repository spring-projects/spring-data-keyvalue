/*
 * Copyright 2016-2025 the original author or authors.
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

import org.jspecify.annotations.Nullable;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * {@link KeyValuePartTreeQuery} implementation deriving queries from {@link PartTree} using a predefined
 * {@link AbstractQueryCreator} that caches the once created query.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 1.1
 */
public class CachingKeyValuePartTreeQuery extends KeyValuePartTreeQuery {

	private @Nullable KeyValueQuery<?> cachedQuery;

	public CachingKeyValuePartTreeQuery(QueryMethod queryMethod,
			ValueExpressionDelegate valueExpressionDelegate, KeyValueOperations keyValueOperations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
		super(queryMethod, valueExpressionDelegate, keyValueOperations, queryCreator);
	}

	protected KeyValueQuery<?> prepareQuery(Object[] parameters) {

		if (cachedQuery == null) {
			cachedQuery = super.prepareQuery(parameters);
		}

		return prepareQuery(cachedQuery, parameters);
	}
}
