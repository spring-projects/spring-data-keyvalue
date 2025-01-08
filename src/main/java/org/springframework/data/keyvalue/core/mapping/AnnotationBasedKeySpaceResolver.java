/*
 * Copyright 2015-2025 the original author or authors.
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

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
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
public enum AnnotationBasedKeySpaceResolver implements KeySpaceResolver {

	INSTANCE;

	@Override
	@Nullable
	public String resolveKeySpace(Class<?> type) {

		Assert.notNull(type, "Type for keyspace for null");

		Class<?> userClass = ClassUtils.getUserClass(type);
		Object keySpace = getKeySpace(userClass);

		return keySpace != null ? keySpace.toString() : null;
	}

	@Nullable
	private static Object getKeySpace(Class<?> type) {

		MergedAnnotation<KeySpace> annotation = MergedAnnotations
				.from(type, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(KeySpace.class);

		if (annotation.isPresent()) {
			return annotation.getValue("value").orElse(null);
		}

		return null;
	}
}
