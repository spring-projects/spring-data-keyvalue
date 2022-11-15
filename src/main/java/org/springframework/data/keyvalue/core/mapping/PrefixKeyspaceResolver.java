/*
 * Copyright 2022 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * {@link KeySpaceResolver} prefixing the {@literal keyspace} with a static prefix after determining the keyspace from a
 * delegate {@link KeySpaceResolver}.
 *
 * @author Mark Paluch
 * @since 3.1
 */
public class PrefixKeyspaceResolver implements KeySpaceResolver {

	private final String prefix;
	private final KeySpaceResolver delegate;

	public PrefixKeyspaceResolver(String prefix, KeySpaceResolver delegate) {

		Assert.notNull(prefix, "Prefix must not be null");
		Assert.notNull(delegate, "Delegate KeySpaceResolver must not be null");

		this.prefix = prefix;
		this.delegate = delegate;
	}

	@Override
	public String resolveKeySpace(Class<?> type) {
		return prefix + delegate.resolveKeySpace(type);
	}

}
