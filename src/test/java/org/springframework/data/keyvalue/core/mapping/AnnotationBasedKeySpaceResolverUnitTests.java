/*
 * Copyright 2014 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.TypeWithDirectKeySpaceAnnotation;
import org.springframework.data.keyvalue.TypeWithInhteritedPersistentAnnotationNotHavingKeySpace;
import org.springframework.data.keyvalue.TypeWithPersistentAnnotationNotHavingKeySpace;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * Unit tests for {@link AnnotationBasedKeySpaceResolver}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class AnnotationBasedKeySpaceResolverUnitTests {

	private AnnotationBasedKeySpaceResolver resolver;

	@Before
	public void setUp() {
		resolver = AnnotationBasedKeySpaceResolver.INSTANCE;
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void shouldResolveKeySpaceDefaultValueCorrectly() {
		assertThat(resolver.resolveKeySpace(EntityWithDefaultKeySpace.class), is("daenerys"));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void shouldResolveKeySpaceCorrectly() {
		assertThat(resolver.resolveKeySpace(EntityWithSetKeySpace.class), is("viserys"));
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void shouldReturnNullWhenNoKeySpaceFoundOnComposedPersistentAnnotation() {
		assertThat(resolver.resolveKeySpace(TypeWithInhteritedPersistentAnnotationNotHavingKeySpace.class), nullValue());
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void shouldReturnNullWhenPersistentIsFoundOnNonComposedAnnotation() {
		assertThat(resolver.resolveKeySpace(TypeWithPersistentAnnotationNotHavingKeySpace.class), nullValue());
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void shouldReturnNullWhenPersistentIsNotFound() {
		assertThat(resolver.resolveKeySpace(TypeWithoutKeySpace.class), nullValue());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void shouldResolveInheritedKeySpaceCorrectly() {
		assertThat(resolver.resolveKeySpace(EntityWithInheritedKeySpace.class), is("viserys"));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void shouldResolveDirectKeySpaceAnnotationCorrectly() {
		assertThat(resolver.resolveKeySpace(TypeWithDirectKeySpaceAnnotation.class), is("rhaegar"));
	}

	@PersistentAnnotationWithExplicitKeySpace
	static class EntityWithDefaultKeySpace {

	}

	@PersistentAnnotationWithExplicitKeySpace(firstname = "viserys")
	static class EntityWithSetKeySpace {

	}

	static class EntityWithInheritedKeySpace extends EntityWithSetKeySpace {

	}

	@Persistent
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	static @interface PersistentAnnotationWithExplicitKeySpace {

		@KeySpace
		String firstname() default "daenerys";

		String lastnamne() default "targaryen";
	}

	static class TypeWithoutKeySpace {

		String foo;

	}
}
