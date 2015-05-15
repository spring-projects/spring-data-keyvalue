/*
 * Copyright 2015 the original author or authors.
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

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link CachingKeySpaceResolver} decorates a given {@link KeySpaceResolver} and returns known keyspaces from a local
 * cache.
 * 
 * @author Christoph Strobl
 */
public class CachingKeySpaceResolver implements KeySpaceResolver {

	private final KeySpaceResolver delegate;
	private final ConcurrentHashMap<Class<?>, String> keySpaceCache = new ConcurrentHashMap<Class<?>, String>();

	/**
	 * @param delegate must not be {@literal null}
	 */
	public CachingKeySpaceResolver(KeySpaceResolver delegate) {

		Assert.notNull(delegate, "Delegate must not be null!");

		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeySpaceResolver#resolveKeySpace(java.lang.Object)
	 */
	@Override
	public String resolveKeySpace(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");
		Class<?> userClass = ClassUtils.getUserClass(type);

		String potentialKeySpace = keySpaceCache.get(userClass);
		if (potentialKeySpace != null) {
			return potentialKeySpace;
		}

		String keySpace = delegate.resolveKeySpace(type);
		keySpaceCache.put(userClass, keySpace);
		return keySpace;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeySpaceResolver#getFallbackKeySpace(java.lang.Class)
	 */
	@Override
	public String getFallbackKeySpace(Class<?> type) {
		return delegate.getFallbackKeySpace(type);
	}

}
