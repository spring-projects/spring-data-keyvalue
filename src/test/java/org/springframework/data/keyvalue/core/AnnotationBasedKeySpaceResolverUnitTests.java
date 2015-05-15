/*
 * Copyright 2014 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.KeyValueTemplateUnitTests.AliasedEntity;
import org.springframework.data.keyvalue.core.KeyValueTemplateUnitTests.ClassWithDirectKeySpaceAnnotation;
import org.springframework.data.keyvalue.core.KeyValueTemplateUnitTests.EntityWithPersistentAnnotation;
import org.springframework.data.keyvalue.core.KeyValueTemplateUnitTests.Foo;

/**
 * Unit tests for {@link KeySpaceUtils}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public class AnnotationBasedKeySpaceResolverUnitTests {

	private AnnotationBasedKeySpaceResolver resolver;

	@Before
	public void setUp() {
		resolver = new AnnotationBasedKeySpaceResolver();
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
	public void shouldReturnClassNameWhenNoKeySpaceFoundOnComposedPersistentAnnotation() {
		assertThat(resolver.resolveKeySpace(AliasedEntity.class), is(AliasedEntity.class.getName()));
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void shouldReturnClassNameWhenPersistentIsFoundOnNonComposedAnnotation() {
		assertThat(resolver.resolveKeySpace(EntityWithPersistentAnnotation.class),
				is(EntityWithPersistentAnnotation.class.getName()));
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void shouldReturnClassNameWhenPersistentIsNotFound() {
		assertThat(resolver.resolveKeySpace(Foo.class), is(Foo.class.getName()));
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
		assertThat(resolver.resolveKeySpace(ClassWithDirectKeySpaceAnnotation.class), is("rhaegar"));
	}

	/**
	 * @see DATAKV-105
	 */
	@Test
	public void getFallbackKeySpaceShouldReturnClassName() {
		assertThat(resolver.getFallbackKeySpace(String.class), is(String.class.getName()));
	}

	/**
	 * @see DATAKV-105
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getFallbackKeySpaceShouldThrowExceptionWhenTypeIsNull() {
		resolver.getFallbackKeySpace(null);
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

	@Persistent
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	static @interface ExplicitKeySpace {

		@KeySpace
		String name() default "";
	}
}
