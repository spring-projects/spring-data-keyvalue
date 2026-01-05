/*
 * Copyright 2014-present the original author or authors.
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
package org.springframework.data.keyvalue.annotation;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.Persistent;

/**
 * Marker interface for methods with {@link Persistent} annotations indicating the presence of a dedicated keyspace the
 * entity should reside in. If present the value will be picked up for resolving the keyspace. The {@link #value()}
 * attribute supports Value Expressions to dynamically resolve the keyspace based on a per-operation basis.
 *
 * <pre class="code">
 * &#64;Persistent
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * &#64;Target({ ElementType.TYPE })
 * static @interface CacheCentricAnnotation {
 *
 * 	&#64;AliasFor(annotation = KeySpace.class, attribute = "value")
 * 	String cacheRegion() default "";
 * }
 *
 * &#64;CacheCentricAnnotation(cacheRegion = "customers")
 * class Customer {
 * 	// ...
 * }
 * </pre>
 *
 * Can also be directly used on types to indicate the keyspace.
 *
 * <pre class="code">
 * &#64;KeySpace("persons")
 * public class Foo {
 *
 * }
 * </pre>
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD, TYPE })
public @interface KeySpace {

	/**
	 * @return dedicated keyspace the entity should reside in.
	 */
	String value() default "";
}
