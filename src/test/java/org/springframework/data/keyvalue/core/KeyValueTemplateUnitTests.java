/*
 * Copyright 2014-2025 the original author or authors.
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
package org.springframework.data.keyvalue.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.SubclassOfTypeWithCustomComposedKeySpaceAnnotation;
import org.springframework.data.keyvalue.TypeWithCustomComposedKeySpaceAnnotationUsingAliasFor;
import org.springframework.data.keyvalue.core.event.KeyValueEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.AfterDeleteEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.AfterDropKeySpaceEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.AfterGetEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.AfterInsertEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.AfterUpdateEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.BeforeDeleteEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.BeforeGetEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.BeforeInsertEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.BeforeUpdateEvent;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KeyValueTemplateUnitTests {

	private static final Foo FOO_ONE = new Foo("one");
	private static final Foo FOO_TWO = new Foo("two");
	private static final TypeWithCustomComposedKeySpaceAnnotationUsingAliasFor ALIASED_USING_ALIAS_FOR = new TypeWithCustomComposedKeySpaceAnnotationUsingAliasFor(
			"super");
	private static final SubclassOfTypeWithCustomComposedKeySpaceAnnotation SUBCLASS_OF_ALIASED_USING_ALIAS_FOR = new SubclassOfTypeWithCustomComposedKeySpaceAnnotation(
			"sub");
	private static final KeyValueQuery<String> STRING_QUERY = new KeyValueQuery<>("foo == 'two'");

	private @Mock KeyValueAdapter adapterMock;
	private KeyValueTemplate template;
	private @Mock ApplicationEventPublisher publisherMock;

	@BeforeEach
	void setUp() {
		when(adapterMock.getMappingContext()).thenReturn(new KeyValueMappingContext());
		this.template = new KeyValueTemplate(adapterMock);
		this.template.setApplicationEventPublisher(publisherMock);
	}

	@Test // DATACMNS-525
	void shouldThrowExceptionWhenCreatingNewTempateWithNullAdapter() {
		assertThatIllegalArgumentException().isThrownBy(() -> new KeyValueTemplate(null));
	}

	@Test // DATACMNS-525
	void shouldThrowExceptionWhenCreatingNewTempateWithNullMappingContext() {
		assertThatIllegalArgumentException().isThrownBy(() -> new KeyValueTemplate(adapterMock, null));
	}

	@Test // DATACMNS-525
	void insertShouldLookUpValuesBeforeInserting() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).contains("1", Foo.class.getName());
	}

	@Test // DATACMNS-525
	void insertShouldInsertUseClassNameAsDefaultKeyspace() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).put("1", FOO_ONE, Foo.class.getName());
	}

	@Test // DATACMNS-225
	void insertShouldReturnInsertedObject() {

		ClassWithStringId object = new ClassWithStringId();

		assertThat(template.insert(object)).isEqualTo(object);
		assertThat(template.insert("1", object)).isEqualTo(object);
	}

	@Test // DATACMNS-525
	void insertShouldThrowExceptionWhenObectWithIdAlreadyExists() {

		when(adapterMock.contains(anyString(), anyString())).thenReturn(true);

		assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> template.insert("1", FOO_ONE));
	}

	@Test // DATACMNS-525
	void insertShouldThrowExceptionForNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.insert(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	void insertShouldThrowExceptionForNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.insert("some-id", null));
	}

	@Test // DATACMNS-525
	void insertShouldGenerateId() {

		ClassWithStringId target = template.insert(new ClassWithStringId());

		assertThat(target.id).isNotNull();
	}

	@Test // DATACMNS-525
	void insertShouldThrowErrorWhenIdCannotBeResolved() {
		assertThatIllegalStateException().isThrownBy(() -> template.insert(FOO_ONE));
	}

	@Test // DATACMNS-525
	void insertShouldReturnSameInstanceGenerateId() {

		ClassWithStringId source = new ClassWithStringId();
		ClassWithStringId target = template.insert(source);

		assertThat(target).isSameAs(source);
	}

	@Test // DATACMNS-525
	void insertShouldRespectExistingId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "one";

		template.insert(source);

		verify(adapterMock, times(1)).put("one", source, ClassWithStringId.class.getName());
	}

	@Test // DATACMNS-525
	void findByIdShouldReturnOptionalEmptyWhenNoElementsPresent() {
		assertThat(template.findById("1", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	void findByIdShouldReturnObjectWithMatchingIdAndType() {

		template.findById("1", Foo.class);

		verify(adapterMock, times(1)).get("1", Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525, DATAKV-187
	void findByIdShouldThrowExceptionWhenGivenNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.findById(null, Foo.class));
	}

	@Test // DATACMNS-525
	void findAllOfShouldReturnEntireCollection() {

		template.findAll(Foo.class);

		verify(adapterMock, times(1)).getAllOf(Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525
	void findAllOfShouldThrowExceptionWhenGivenNullType() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.findAll(null));
	}

	@Test // DATACMNS-525
	void findShouldCallFindOnAdapterToResolveMatching() {

		template.find(STRING_QUERY, Foo.class);

		verify(adapterMock, times(1)).find(STRING_QUERY, Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525
	@SuppressWarnings("rawtypes")
	void findInRangeShouldRespectOffset() {

		ArgumentCaptor<KeyValueQuery> captor = ArgumentCaptor.forClass(KeyValueQuery.class);

		template.findInRange(1, 5, Foo.class);

		verify(adapterMock, times(1)).find(captor.capture(), eq(Foo.class.getName()), eq(Foo.class));
		assertThat(captor.getValue().getOffset()).isEqualTo(1L);
		assertThat(captor.getValue().getRows()).isEqualTo(5);
		assertThat(captor.getValue().getCriteria()).isNull();
	}

	@Test // DATACMNS-525
	void updateShouldReplaceExistingObject() {

		template.update("1", FOO_TWO);

		verify(adapterMock, times(1)).put("1", FOO_TWO, Foo.class.getName());
	}

	@Test // DATAKV-225
	void updateShouldReturnUpdatedObject() {

		ClassWithStringId object = new ClassWithStringId();
		object.id = "foo";

		assertThat(template.update(object)).isEqualTo(object);
		assertThat(template.update("1", object)).isEqualTo(object);
	}

	@Test // DATACMNS-525
	void updateShouldThrowExceptionWhenGivenNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.update(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	void updateShouldThrowExceptionWhenGivenNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.update("1", null));
	}

	@Test // DATACMNS-525
	void updateShouldUseExtractedIdInformation() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.update(source);

		verify(adapterMock, times(1)).put(source.id, source, ClassWithStringId.class.getName());
	}

	@Test // DATACMNS-525
	void updateShouldThrowErrorWhenIdInformationCannotBeExtracted() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(() -> template.update(FOO_ONE));
	}

	@Test // DATACMNS-525
	void deleteShouldRemoveObjectCorrectly() {

		template.delete("1", Foo.class);

		verify(adapterMock, times(1)).delete("1", Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525
	void deleteRemovesObjectUsingExtractedId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.delete(source);

		verify(adapterMock, times(1)).delete("some-id", ClassWithStringId.class.getName(), ClassWithStringId.class);
	}

	@Test // DATACMNS-525
	void deleteThrowsExceptionWhenIdCannotBeExctracted() {
		assertThatIllegalStateException().isThrownBy(() -> template.delete(FOO_ONE));
	}

	@Test // DATACMNS-525
	void countShouldReturnZeroWhenNoElementsPresent() {
		template.count(Foo.class);
	}

	@Test // DATACMNS-525
	void countShouldReturnCollectionSize() {

		when(adapterMock.count(Foo.class.getName())).thenReturn(2L);

		assertThat(template.count(Foo.class)).isEqualTo(2L);
	}

	@Test // DATACMNS-525
	void countShouldThrowErrorOnNullType() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.count(null));
	}

	@Test // DATACMNS-525
	void insertShouldRespectTypeAlias() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@Test // DATACMNS-525
	void insertShouldRespectTypeAliasOnSubClass() {

		template.insert("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test // DATACMNS-525
	void findAllOfShouldRespectTypeAliasAndFilterNonMatchingTypes() {

		Collection foo = Arrays.asList(ALIASED_USING_ALIAS_FOR, SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);
		when(adapterMock.getAllOf("aliased", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR.getClass())).thenReturn(foo);

		assertThat((Iterable) template.findAll(SUBCLASS_OF_ALIASED_USING_ALIAS_FOR.getClass()))
				.contains(SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);
	}

	@Test // DATACMNS-525
	void insertSouldRespectTypeAliasAndFilterNonMatching() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);
		assertThat(template.findById("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR.getClass())).isEmpty();
	}

	@Test // DATACMNS-525
	void setttingNullPersistenceExceptionTranslatorShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.setExceptionTranslator(null));
	}

	@Test // DATAKV-91
	void shouldNotPublishEventWhenNoApplicationContextSet() {

		template.setApplicationEventPublisher(null);

		template.insert("1", FOO_ONE);

		verifyNoInteractions(publisherMock);
	}

	@Test // DATAKV-104
	void shouldNotPublishEventsWhenEventsToPublishIsSetToNull() {

		template.setEventTypesToPublish(null);

		template.insert("1", FOO_ONE);

		verifyNoInteractions(publisherMock);
	}

	@Test // DATAKV-104

	void shouldNotPublishEventsWhenEventsToPublishIsSetToEmptyList() {

		template.setEventTypesToPublish(Collections.emptySet());

		template.insert("1", FOO_ONE);

		verifyNoInteractions(publisherMock);
	}

	@Test // DATAKV-104
	void shouldPublishEventsByDefault() {

		template.insert("1", FOO_ONE);

		verify(publisherMock, atLeastOnce()).publishEvent(any());
	}

	@Test // DATAKV-91, DATAKV-104

	void shouldNotPublishEventWhenNotExplicitlySetForPublication() {

		setEventsToPublish(BeforeDeleteEvent.class);

		template.insert("1", FOO_ONE);

		verifyNoInteractions(publisherMock);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishBeforeInsertEventCorrectly() {

		setEventsToPublish(BeforeInsertEvent.class);

		template.insert("1", FOO_ONE);

		ArgumentCaptor<BeforeInsertEvent> captor = ArgumentCaptor.forClass(BeforeInsertEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishAfterInsertEventCorrectly() {

		setEventsToPublish(AfterInsertEvent.class);

		template.insert("1", FOO_ONE);

		ArgumentCaptor<AfterInsertEvent> captor = ArgumentCaptor.forClass(AfterInsertEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishBeforeUpdateEventCorrectly() {

		setEventsToPublish(BeforeUpdateEvent.class);

		template.update("1", FOO_ONE);

		ArgumentCaptor<BeforeUpdateEvent> captor = ArgumentCaptor.forClass(BeforeUpdateEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishAfterUpdateEventCorrectly() {

		setEventsToPublish(AfterUpdateEvent.class);

		template.update("1", FOO_ONE);

		ArgumentCaptor<AfterUpdateEvent> captor = ArgumentCaptor.forClass(AfterUpdateEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishBeforeDeleteEventCorrectly() {

		setEventsToPublish(BeforeDeleteEvent.class);

		template.delete("1", FOO_ONE.getClass());

		ArgumentCaptor<BeforeDeleteEvent> captor = ArgumentCaptor.forClass(BeforeDeleteEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishAfterDeleteEventCorrectly() {

		setEventsToPublish(AfterDeleteEvent.class);
		when(adapterMock.delete(eq("1"), eq(FOO_ONE.getClass().getName()), eq(Foo.class))).thenReturn(FOO_ONE);

		template.delete("1", FOO_ONE.getClass());

		ArgumentCaptor<AfterDeleteEvent> captor = ArgumentCaptor.forClass(AfterDeleteEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishBeforeGetEventCorrectly() {

		setEventsToPublish(BeforeGetEvent.class);

		when(adapterMock.get(eq("1"), eq(FOO_ONE.getClass().getName()))).thenReturn(FOO_ONE);

		template.findById("1", FOO_ONE.getClass());

		ArgumentCaptor<BeforeGetEvent> captor = ArgumentCaptor.forClass(BeforeGetEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
	}

	@Test // DATAKV-91, DATAKV-104
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishAfterGetEventCorrectly() {

		setEventsToPublish(AfterGetEvent.class);

		when(adapterMock.get(eq("1"), eq(FOO_ONE.getClass().getName()), eq(Foo.class))).thenReturn(FOO_ONE);

		template.findById("1", FOO_ONE.getClass());

		ArgumentCaptor<AfterGetEvent> captor = ArgumentCaptor.forClass(AfterGetEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
		assertThat(captor.getValue().getPayload()).isEqualTo(FOO_ONE);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes" })
	void shouldPublishDropKeyspaceEventCorrectly() {

		setEventsToPublish(AfterDropKeySpaceEvent.class);

		template.delete(FOO_ONE.getClass());

		ArgumentCaptor<AfterDropKeySpaceEvent> captor = ArgumentCaptor.forClass(AfterDropKeySpaceEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
	}

	@Test // DATAKV-129
	void insertShouldRespectTypeAliasUsingAliasFor() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@SafeVarargs
	@SuppressWarnings("rawtypes")
	private final void setEventsToPublish(Class<? extends KeyValueEvent>... events) {
		template.setEventTypesToPublish(new HashSet<>(Arrays.asList(events)));
	}

	static class Foo {

		String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		public String getFoo() {
			return this.foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}
	}

	class Bar {

		String bar;

		public Bar(String bar) {
			this.bar = bar;
		}

		public String getBar() {
			return this.bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}
	}

	static class ClassWithStringId {

		@Id String id;
		String value;

		public String getId() {
			return this.id;
		}

		public String getValue() {
			return this.value;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
