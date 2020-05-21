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

import java.util.function.Predicate;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * {@link CriteriaAccessor} implementation capable of {@link Predicate}s.
 *
 * @author Marcel Overdijk
 */
class PredicateCriteriaAccessor implements CriteriaAccessor<Predicate> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.CriteriaAccessor#resolve(org.springframework.data.keyvalue.core.query.KeyValueQuery)
	 */
	@Override
	public Predicate resolve(KeyValueQuery<?> query) {

		if (query.getCriteria() == null) {
			return null;
		}

		if (query.getCriteria() instanceof Predicate) {
			return (Predicate) query.getCriteria();
		}

		throw new IllegalArgumentException("Cannot create Predicate for " + query.getCriteria());
	}
}
