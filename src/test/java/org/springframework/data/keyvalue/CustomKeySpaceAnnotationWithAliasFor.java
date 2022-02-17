/*
 * Copyright 2016-2022 the original author or authors.
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
package org.springframework.data.keyvalue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * Custom composed {@link Persistent} annotation using {@link AliasFor} on name attribute.
 *
 * @author Christoph Strobl
 */
@Persistent
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@KeySpace
public @interface CustomKeySpaceAnnotationWithAliasFor {

	@AliasFor(annotation = KeySpace.class, attribute = "value")
	String name() default "";
}
