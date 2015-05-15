/*
 * Copyright 2014-2015 the original author or authors.
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
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.TypeWithCustomComposedKeySpaceAnnotation;
import org.springframework.data.keyvalue.SubclassOfTypeWithCustomComposedKeySpaceAnnotation;
import org.springframework.data.keyvalue.core.event.KeyValueEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.DeleteEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.DropKeyspaceEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.GetEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.InsertEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.Type;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.UpdateEvent;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyValueTemplateUnitTests {

	public @Rule ExpectedException exception = ExpectedException.none();

	private static final Foo FOO_ONE = new Foo("one");
	private static final Foo FOO_TWO = new Foo("two");
	private static final TypeWithCustomComposedKeySpaceAnnotation ALIASED = new TypeWithCustomComposedKeySpaceAnnotation("super");
	private static final SubclassOfTypeWithCustomComposedKeySpaceAnnotation SUBCLASS_OF_ALIASED = new SubclassOfTypeWithCustomComposedKeySpaceAnnotation("sub");

	private static final KeyValueQuery<String> STRING_QUERY = new KeyValueQuery<String>("foo == 'two'");

	private @Mock KeyValueAdapter adapterMock;
	private KeyValueTemplate template;
	private @Mock ApplicationContext ctxMock;

	@Before
	public void setUp() throws InstantiationException, IllegalAccessException {
		this.template = new KeyValueTemplate(adapterMock);
		this.template.setApplicationContext(ctxMock);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenCreatingNewTempateWithNullAdapter() {
		new KeyValueTemplate(null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenCreatingNewTempateWithNullMappingContext() {
		new KeyValueTemplate(adapterMock, null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldLookUpValuesBeforeInserting() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).contains("1", Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldInsertUseClassNameAsDefaultKeyspace() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).put("1", FOO_ONE, Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldThrowExceptionWhenObectWithIdAlreadyExists() {

		exception.expect(DuplicateKeyException.class);
		exception.expectMessage("id 1");

		when(adapterMock.contains(anyString(), anyString())).thenReturn(true);

		template.insert("1", FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void insertShouldThrowExceptionForNullId() {
		template.insert(null, FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void insertShouldThrowExceptionForNullObject() {
		template.insert("some-id", null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldGenerateId() {

		ClassWithStringId target = template.insert(new ClassWithStringId());

		assertThat(target.id, notNullValue());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void insertShouldThrowErrorWhenIdCannotBeResolved() {
		template.insert(FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldReturnSameInstanceGenerateId() {

		ClassWithStringId source = new ClassWithStringId();
		ClassWithStringId target = template.insert(source);

		assertThat(target, sameInstance(source));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldRespectExistingId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "one";

		template.insert(source);

		verify(adapterMock, times(1)).put("one", source, ClassWithStringId.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findByIdShouldReturnNullWhenNoElementsPresent() {
		assertNull(template.findById("1", Foo.class));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findByIdShouldReturnObjectWithMatchingIdAndType() {

		template.findById("1", Foo.class);

		verify(adapterMock, times(1)).get("1", Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void findByIdShouldThrowExceptionWhenGivenNullId() {
		template.findById((Serializable) null, Foo.class);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findAllOfShouldReturnEntireCollection() {

		template.findAll(Foo.class);

		verify(adapterMock, times(1)).getAllOf(Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void findAllOfShouldThrowExceptionWhenGivenNullType() {
		template.findAll(null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void findShouldCallFindOnAdapterToResolveMatching() {

		template.find(STRING_QUERY, Foo.class);

		verify(adapterMock, times(1)).find(STRING_QUERY, Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void findInRangeShouldRespectOffset() {

		ArgumentCaptor<KeyValueQuery> captor = ArgumentCaptor.forClass(KeyValueQuery.class);

		template.findInRange(1, 5, Foo.class);

		verify(adapterMock, times(1)).find(captor.capture(), eq(Foo.class.getName()));
		assertThat(captor.getValue().getOffset(), is(1));
		assertThat(captor.getValue().getRows(), is(5));
		assertThat(captor.getValue().getCritieria(), nullValue());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void updateShouldReplaceExistingObject() {

		template.update("1", FOO_TWO);

		verify(adapterMock, times(1)).put("1", FOO_TWO, Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void updateShouldThrowExceptionWhenGivenNullId() {
		template.update(null, FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void updateShouldThrowExceptionWhenGivenNullObject() {
		template.update("1", null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void updateShouldUseExtractedIdInformation() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.update(source);

		verify(adapterMock, times(1)).put(source.id, source, ClassWithStringId.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void updateShouldThrowErrorWhenIdInformationCannotBeExtracted() {
		template.update(FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void deleteShouldRemoveObjectCorrectly() {

		template.delete("1", Foo.class);

		verify(adapterMock, times(1)).delete("1", Foo.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void deleteRemovesObjectUsingExtractedId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.delete(source);

		verify(adapterMock, times(1)).delete("some-id", ClassWithStringId.class.getName());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void deleteThrowsExceptionWhenIdCannotBeExctracted() {
		template.delete(FOO_ONE);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void countShouldReturnZeroWhenNoElementsPresent() {
		template.count(Foo.class);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void countShouldReturnCollectionSize() {

		when(adapterMock.count(Foo.class.getName())).thenReturn(2L);

		assertThat(template.count(Foo.class), is(2L));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void countShouldThrowErrorOnNullType() {
		template.count(null);
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldRespectTypeAlias() {

		template.insert("1", ALIASED);

		verify(adapterMock, times(1)).put("1", ALIASED, "aliased");
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertShouldRespectTypeAliasOnSubClass() {

		template.insert("1", SUBCLASS_OF_ALIASED);

		verify(adapterMock, times(1)).put("1", SUBCLASS_OF_ALIASED, "aliased");
	}

	/**
	 * @see DATACMNS-525
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void findAllOfShouldRespectTypeAliasAndFilterNonMatchingTypes() {

		Collection foo = Arrays.asList(ALIASED, SUBCLASS_OF_ALIASED);
		when(adapterMock.getAllOf("aliased")).thenReturn(foo);

		assertThat(template.findAll(SUBCLASS_OF_ALIASED.getClass()), containsInAnyOrder(SUBCLASS_OF_ALIASED));
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test
	public void insertSouldRespectTypeAliasAndFilterNonMatching() {

		template.insert("1", ALIASED);
		assertThat(template.findById("1", SUBCLASS_OF_ALIASED.getClass()), nullValue());
	}

	/**
	 * @see DATACMNS-525
	 */
	@Test(expected = IllegalArgumentException.class)
	public void setttingNullPersistenceExceptionTranslatorShouldThrowException() {
		template.setExceptionTranslator(null);
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldNotPublishEventWhenNoApplicationContextSet() {

		template.setApplicationContext(null);
		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_DELETE,
				KeyValueEvent.Type.AFTER_INSERT, KeyValueEvent.Type.AFTER_UPDATE, KeyValueEvent.Type.BEFORE_DELETE,
				KeyValueEvent.Type.BEFORE_INSERT, KeyValueEvent.Type.BEFORE_UPDATE)));

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(ctxMock);
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldNotPublishEventWhenNotExplicitlySetForPublication() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_DELETE)));

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(ctxMock);
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishBeforeInsertEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.BEFORE_INSERT)));

		template.insert("1", FOO_ONE);

		ArgumentCaptor<InsertEvent> captor = ArgumentCaptor.forClass(InsertEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.BEFORE_INSERT));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishAfterInsertEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_INSERT)));

		template.insert("1", FOO_ONE);

		ArgumentCaptor<InsertEvent> captor = ArgumentCaptor.forClass(InsertEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.AFTER_INSERT));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishBeforeUpdateEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.BEFORE_UPDATE)));

		template.update("1", FOO_ONE);

		ArgumentCaptor<UpdateEvent> captor = ArgumentCaptor.forClass(UpdateEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.BEFORE_UPDATE));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishAfterUpdateEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_UPDATE)));

		template.update("1", FOO_ONE);

		ArgumentCaptor<UpdateEvent> captor = ArgumentCaptor.forClass(UpdateEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.AFTER_UPDATE));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishBeforeDeleteEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.BEFORE_DELETE)));

		template.delete("1", FOO_ONE.getClass());

		ArgumentCaptor<DeleteEvent> captor = ArgumentCaptor.forClass(DeleteEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.BEFORE_DELETE));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), nullValue());
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishAfterDeleteEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_DELETE)));
		when(adapterMock.delete(eq("1"), eq(FOO_ONE.getClass().getName()))).thenReturn(FOO_ONE);

		template.delete("1", FOO_ONE.getClass());

		ArgumentCaptor<DeleteEvent> captor = ArgumentCaptor.forClass(DeleteEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.AFTER_DELETE));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishBeforeGetEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.BEFORE_GET)));
		when(adapterMock.get(eq("1"), eq(FOO_ONE.getClass().getName()))).thenReturn(FOO_ONE);

		template.findById("1", FOO_ONE.getClass());

		ArgumentCaptor<GetEvent> captor = ArgumentCaptor.forClass(GetEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.BEFORE_GET));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), nullValue());
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishAfterGetEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_GET)));
		when(adapterMock.get(eq("1"), eq(FOO_ONE.getClass().getName()))).thenReturn(FOO_ONE);

		template.findById("1", FOO_ONE.getClass());

		ArgumentCaptor<GetEvent> captor = ArgumentCaptor.forClass(GetEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.AFTER_GET));
		assertThat(captor.getValue().getId(), is((Serializable) "1"));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
		assertThat(captor.getValue().getValue(), is((Object) FOO_ONE));
	}

	/**
	 * @see DATAKV-91
	 */
	@Test
	public void shouldPublishDropKeyspaceEventCorrectly() {

		template.setEventTypesToPublish(new HashSet<KeyValueEvent.Type>(Arrays.asList(KeyValueEvent.Type.AFTER_DELETE)));

		template.delete(FOO_ONE.getClass());

		ArgumentCaptor<DropKeyspaceEvent> captor = ArgumentCaptor.forClass(DropKeyspaceEvent.class);

		verify(ctxMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(ctxMock);

		assertThat(captor.getValue().getType(), is(Type.AFTER_DELETE));
		assertThat(captor.getValue().getKeyspace(), is(Foo.class.getName()));
	}

	static class Foo {

		String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		public String getFoo() {
			return foo;
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.foo);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Foo)) {
				return false;
			}
			Foo other = (Foo) obj;
			return ObjectUtils.nullSafeEquals(this.foo, other.foo);
		}

	}

	class Bar {

		String bar;

		public Bar(String bar) {
			this.bar = bar;
		}

		public String getBar() {
			return bar;
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.bar);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Bar)) {
				return false;
			}
			Bar other = (Bar) obj;
			return ObjectUtils.nullSafeEquals(this.bar, other.bar);
		}

	}

	static class ClassWithStringId {

		@Id String id;
		String value;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ObjectUtils.nullSafeHashCode(this.id);
			result = prime * result + ObjectUtils.nullSafeHashCode(this.value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ClassWithStringId)) {
				return false;
			}
			ClassWithStringId other = (ClassWithStringId) obj;
			if (!ObjectUtils.nullSafeEquals(this.id, other.id)) {
				return false;
			}
			if (!ObjectUtils.nullSafeEquals(this.value, other.value)) {
				return false;
			}
			return true;
		}

	}
}
