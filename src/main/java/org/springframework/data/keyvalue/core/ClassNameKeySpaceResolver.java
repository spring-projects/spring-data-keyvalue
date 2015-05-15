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

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Most trivial implementation of {@link KeySpaceResolver} returning the {@link Class#getName()}.
 * 
 * @author Christoph Strobl
 */
public enum ClassNameKeySpaceResolver implements KeySpaceResolver {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeySpaceResolver#resolveKeySpace(java.lang.Class)
	 */
	@Override
	public String resolveKeySpace(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");
		return ClassUtils.getUserClass(type).getName();
	}

}
