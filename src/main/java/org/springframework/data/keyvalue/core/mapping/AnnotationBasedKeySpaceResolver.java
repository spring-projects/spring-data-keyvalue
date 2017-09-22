/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.data.keyvalue.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link AnnotationBasedKeySpaceResolver} looks up {@link Persistent} and checks for presence of either meta or direct
 * usage of {@link KeySpace}. If non found it will default the keyspace to {@link Class#getName()}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
enum AnnotationBasedKeySpaceResolver implements KeySpaceResolver {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.core.KeySpaceResolver#resolveKeySpace(java.lang.Class)
	 */
	@Override
	@Nullable
	public String resolveKeySpace(Class<?> type) {

		Assert.notNull(type, "Type for keyspace for null!");

		Class<?> userClass = ClassUtils.getUserClass(type);
		Object keySpace = getKeySpace(userClass);

		return keySpace != null ? keySpace.toString() : null;
	}

	@Nullable
	private static Object getKeySpace(Class<?> type) {

		KeySpace keyspace = AnnotatedElementUtils.findMergedAnnotation(type, KeySpace.class);

		if (keyspace != null) {
			return AnnotationUtils.getValue(keyspace);
		}

		return null;
	}
}
