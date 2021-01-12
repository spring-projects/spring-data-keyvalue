/*
 * Copyright 2014-2021 the original author or authors.
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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class KeyValueTemplateUnitTests {

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

	@Before
	public void setUp() {
		this.template = new KeyValueTemplate(adapterMock);
		this.template.setApplicationEventPublisher(publisherMock);
	}

	@Test // DATACMNS-525
	public void shouldThrowExceptionWhenCreatingNewTempateWithNullAdapter() {
		assertThatIllegalArgumentException().isThrownBy(() -> new KeyValueTemplate(null));
	}

	@Test // DATACMNS-525
	public void shouldThrowExceptionWhenCreatingNewTempateWithNullMappingContext() {
		assertThatIllegalArgumentException().isThrownBy(() -> new KeyValueTemplate(adapterMock, null));
	}

	@Test // DATACMNS-525
	public void insertShouldLookUpValuesBeforeInserting() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).contains("1", Foo.class.getName());
	}

	@Test // DATACMNS-525
	public void insertShouldInsertUseClassNameAsDefaultKeyspace() {

		template.insert("1", FOO_ONE);

		verify(adapterMock, times(1)).put("1", FOO_ONE, Foo.class.getName());
	}

	@Test // DATACMNS-225
	public void insertShouldReturnInsertedObject() {

		ClassWithStringId object = new ClassWithStringId();

		assertThat(template.insert(object)).isEqualTo(object);
		assertThat(template.insert("1", object)).isEqualTo(object);
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExceptionWhenObectWithIdAlreadyExists() {

		when(adapterMock.contains(anyString(), anyString())).thenReturn(true);

		assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> template.insert("1", FOO_ONE));
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExceptionForNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.insert(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	public void insertShouldThrowExceptionForNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.insert("some-id", null));
	}

	@Test // DATACMNS-525
	public void insertShouldGenerateId() {

		ClassWithStringId target = template.insert(new ClassWithStringId());

		assertThat(target.id).isNotNull();
	}

	@Test // DATACMNS-525
	public void insertShouldThrowErrorWhenIdCannotBeResolved() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.insert(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void insertShouldReturnSameInstanceGenerateId() {

		ClassWithStringId source = new ClassWithStringId();
		ClassWithStringId target = template.insert(source);

		assertThat(target).isSameAs(source);
	}

	@Test // DATACMNS-525
	public void insertShouldRespectExistingId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "one";

		template.insert(source);

		verify(adapterMock, times(1)).put("one", source, ClassWithStringId.class.getName());
	}

	@Test // DATACMNS-525
	public void findByIdShouldReturnOptionalEmptyWhenNoElementsPresent() {
		assertThat(template.findById("1", Foo.class)).isEmpty();
	}

	@Test // DATACMNS-525
	public void findByIdShouldReturnObjectWithMatchingIdAndType() {

		template.findById("1", Foo.class);

		verify(adapterMock, times(1)).get("1", Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525, DATAKV-187
	public void findByIdShouldThrowExceptionWhenGivenNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.findById(null, Foo.class));
	}

	@Test // DATACMNS-525
	public void findAllOfShouldReturnEntireCollection() {

		template.findAll(Foo.class);

		verify(adapterMock, times(1)).getAllOf(Foo.class.getName());
	}

	@Test // DATACMNS-525
	public void findAllOfShouldThrowExceptionWhenGivenNullType() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.findAll(null));
	}

	@Test // DATACMNS-525
	public void findShouldCallFindOnAdapterToResolveMatching() {

		template.find(STRING_QUERY, Foo.class);

		verify(adapterMock, times(1)).find(STRING_QUERY, Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525
	@SuppressWarnings("rawtypes")
	public void findInRangeShouldRespectOffset() {

		ArgumentCaptor<KeyValueQuery> captor = ArgumentCaptor.forClass(KeyValueQuery.class);

		template.findInRange(1, 5, Foo.class);

		verify(adapterMock, times(1)).find(captor.capture(), eq(Foo.class.getName()), eq(Foo.class));
		assertThat(captor.getValue().getOffset()).isEqualTo(1L);
		assertThat(captor.getValue().getRows()).isEqualTo(5);
		assertThat(captor.getValue().getCriteria()).isNull();
	}

	@Test // DATACMNS-525
	public void updateShouldReplaceExistingObject() {

		template.update("1", FOO_TWO);

		verify(adapterMock, times(1)).put("1", FOO_TWO, Foo.class.getName());
	}

	@Test // DATAKV-225
	public void updateShouldReturnUpdatedObject() {

		ClassWithStringId object = new ClassWithStringId();
		object.id = "foo";

		assertThat(template.update(object)).isEqualTo(object);
		assertThat(template.update("1", object)).isEqualTo(object);
	}

	@Test // DATACMNS-525
	public void updateShouldThrowExceptionWhenGivenNullId() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.update(null, FOO_ONE));
	}

	@Test // DATACMNS-525
	public void updateShouldThrowExceptionWhenGivenNullObject() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.update("1", null));
	}

	@Test // DATACMNS-525
	public void updateShouldUseExtractedIdInformation() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.update(source);

		verify(adapterMock, times(1)).put(source.id, source, ClassWithStringId.class.getName());
	}

	@Test // DATACMNS-525
	public void updateShouldThrowErrorWhenIdInformationCannotBeExtracted() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(() -> template.update(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void deleteShouldRemoveObjectCorrectly() {

		template.delete("1", Foo.class);

		verify(adapterMock, times(1)).delete("1", Foo.class.getName(), Foo.class);
	}

	@Test // DATACMNS-525
	public void deleteRemovesObjectUsingExtractedId() {

		ClassWithStringId source = new ClassWithStringId();
		source.id = "some-id";

		template.delete(source);

		verify(adapterMock, times(1)).delete("some-id", ClassWithStringId.class.getName(), ClassWithStringId.class);
	}

	@Test // DATACMNS-525
	public void deleteThrowsExceptionWhenIdCannotBeExctracted() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.delete(FOO_ONE));
	}

	@Test // DATACMNS-525
	public void countShouldReturnZeroWhenNoElementsPresent() {
		template.count(Foo.class);
	}

	@Test // DATACMNS-525
	public void countShouldReturnCollectionSize() {

		when(adapterMock.count(Foo.class.getName())).thenReturn(2L);

		assertThat(template.count(Foo.class)).isEqualTo(2L);
	}

	@Test // DATACMNS-525
	public void countShouldThrowErrorOnNullType() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.count(null));
	}

	@Test // DATACMNS-525
	public void insertShouldRespectTypeAlias() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@Test // DATACMNS-525
	public void insertShouldRespectTypeAliasOnSubClass() {

		template.insert("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test // DATACMNS-525
	public void findAllOfShouldRespectTypeAliasAndFilterNonMatchingTypes() {

		Collection foo = Arrays.asList(ALIASED_USING_ALIAS_FOR, SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);
		when(adapterMock.getAllOf("aliased")).thenReturn(foo);

		assertThat((Iterable) template.findAll(SUBCLASS_OF_ALIASED_USING_ALIAS_FOR.getClass()))
				.contains(SUBCLASS_OF_ALIASED_USING_ALIAS_FOR);
	}

	@Test // DATACMNS-525
	public void insertSouldRespectTypeAliasAndFilterNonMatching() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);
		assertThat(template.findById("1", SUBCLASS_OF_ALIASED_USING_ALIAS_FOR.getClass())).isEmpty();
	}

	@Test // DATACMNS-525
	public void setttingNullPersistenceExceptionTranslatorShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> template.setExceptionTranslator(null));
	}

	@Test // DATAKV-91
	public void shouldNotPublishEventWhenNoApplicationContextSet() {

		template.setApplicationEventPublisher(null);

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(publisherMock);
	}

	@Test // DATAKV-104
	public void shouldNotPublishEventsWhenEventsToPublishIsSetToNull() {

		template.setEventTypesToPublish(null);

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(publisherMock);
	}

	@Test // DATAKV-104
	@SuppressWarnings("rawtypes")
	public void shouldNotPublishEventsWhenEventsToPublishIsSetToEmptyList() {

		template.setEventTypesToPublish(Collections.emptySet());

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(publisherMock);
	}

	@Test // DATAKV-104
	public void shouldPublishEventsByDefault() {

		template.insert("1", FOO_ONE);

		verify(publisherMock, atLeastOnce()).publishEvent(any());
	}

	@Test // DATAKV-91, DATAKV-104
	@SuppressWarnings({ "unchecked", })
	public void shouldNotPublishEventWhenNotExplicitlySetForPublication() {

		setEventsToPublish(BeforeDeleteEvent.class);

		template.insert("1", FOO_ONE);

		verifyZeroInteractions(publisherMock);
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldPublishBeforeInsertEventCorrectly() {

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldPublishAfterInsertEventCorrectly() {

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldPublishBeforeUpdateEventCorrectly() {

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldPublishAfterUpdateEventCorrectly() {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldPublishBeforeDeleteEventCorrectly() {

		setEventsToPublish(BeforeDeleteEvent.class);

		template.delete("1", FOO_ONE.getClass());

		ArgumentCaptor<BeforeDeleteEvent> captor = ArgumentCaptor.forClass(BeforeDeleteEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKey()).isEqualTo("1");
		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
	}

	@Test // DATAKV-91, DATAKV-104, DATAKV-187
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldPublishAfterDeleteEventCorrectly() {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldPublishBeforeGetEventCorrectly() {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldPublishAfterGetEventCorrectly() {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldPublishDropKeyspaceEventCorrectly() {

		setEventsToPublish(AfterDropKeySpaceEvent.class);

		template.delete(FOO_ONE.getClass());

		ArgumentCaptor<AfterDropKeySpaceEvent> captor = ArgumentCaptor.forClass(AfterDropKeySpaceEvent.class);

		verify(publisherMock, times(1)).publishEvent(captor.capture());
		verifyNoMoreInteractions(publisherMock);

		assertThat(captor.getValue().getKeyspace()).isEqualTo(Foo.class.getName());
	}

	@Test // DATAKV-129
	public void insertShouldRespectTypeAliasUsingAliasFor() {

		template.insert("1", ALIASED_USING_ALIAS_FOR);

		verify(adapterMock, times(1)).put("1", ALIASED_USING_ALIAS_FOR, "aliased");
	}

	@SafeVarargs
	@SuppressWarnings("rawtypes")
	private final void setEventsToPublish(Class<? extends KeyValueEvent>... events) {
		template.setEventTypesToPublish(new HashSet<>(Arrays.asList(events)));
	}

	@Data
	@AllArgsConstructor
	static class Foo {

		String foo;
	}

	@Data
	@AllArgsConstructor
	class Bar {

		String bar;
	}

	@Data
	static class ClassWithStringId {

		@Id String id;
		String value;
	}
}
