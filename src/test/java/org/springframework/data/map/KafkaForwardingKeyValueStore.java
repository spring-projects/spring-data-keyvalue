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
package org.springframework.data.map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.keyvalue.core.KeyValueAccessor;
import org.springframework.data.keyvalue.core.KeyValueStore;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class KafkaForwardingKeyValueStore {

	@Mock KafkaOperations kafkaOps;

	KeyValueAccessor accessor;
	KeyValueTemplate template;

	@Before
	public void setUp() {

		this.accessor = new KafkaForwardingKeyValueAccessor(new MapKeyValueAccessor(), kafkaOps);
		template = new KeyValueTemplate(this.accessor);
	}

	@Test
	public void initFormEmptyTopic() {

		Iterable<Map.Entry<Object, Object>> iterable = Collections.emptyList();
		when(kafkaOps.seek(anyString(), anyInt())).thenReturn(iterable);

		KeyValueStore<String, Object> store = template.getKeyValueStore("topic");

		assertThat(store.put("daenerys", "stormborn"), IsNull.nullValue());
		assertThat(store.contains("daenerys"), Is.is(true));

		verify(kafkaOps, times(1)).seek(eq("topic"), anyInt());
		verify(kafkaOps, times(1)).send(eq("topic"), eq("daenerys"), eq("stormborn"));
	}

	@Test
	public void initFormTopic() {

		Map<Object, Object> values = Collections.<Object, Object> singletonMap("daenerys", "stormborn");
		Iterable<Map.Entry<Object, Object>> iterable = values.entrySet();
		when(kafkaOps.seek(anyString(), anyInt())).thenReturn(iterable);

		KeyValueStore<String, Object> store = template.getKeyValueStore("topic");

		assertThat(store.contains("daenerys"), Is.is(true));
		assertThat(store.put("daenerys", "targaryen"), Is.<Object> is("stormborn"));
		assertThat(store.contains("daenerys"), Is.is(true));

		verify(kafkaOps, times(1)).seek(eq("topic"), anyInt());
		verify(kafkaOps, times(1)).send(eq("topic"), eq("daenerys"), eq("targaryen"));
	}

	static class KafkaForwardingKeyValueAccessor implements KeyValueAccessor {

		final KeyValueAccessor accessor;
		final KafkaOperations kafka;

		public KafkaForwardingKeyValueAccessor(KeyValueAccessor accessor, KafkaOperations kafka) {
			super();
			this.accessor = accessor;
			this.kafka = kafka;
		}

		@Override
		public Object put(Serializable id, Object item, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			publishToKafka(keyspace, id, item);
			return accessor.put(id, item, keyspace);
		}

		@Override
		public boolean contains(Serializable id, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			return accessor.contains(id, keyspace);
		}

		@Override
		public Object get(Serializable id, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			return accessor.get(id, keyspace);
		}

		@Override
		public Object delete(Serializable id, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			publishToKafka(keyspace, id, null);
			return accessor.delete(id, keyspace);
		}

		@Override
		public Collection<?> getAllOf(Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			return accessor.getAllOf(keyspace);
		}

		@Override
		public void deleteAllOf(Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			// TODO Auto-generated method stub
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

		@Override
		public Collection<?> find(KeyValueQuery<?> query, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			return accessor.find(query, keyspace);
		}

		@Override
		public long count(KeyValueQuery<?> query, Serializable keyspace) {

			potentiallyInitKeyspace(keyspace);
			return accessor.count(query, keyspace);
		}

		@Override
		public boolean hasKeyspace(Serializable keyspace) {
			return accessor.hasKeyspace(keyspace);
		}

		@Override
		public void destroy() throws Exception {
			// TODO Auto-generated method stub
		}

		private void potentiallyInitKeyspace(Serializable keyspace) {

			if (hasKeyspace(keyspace)) {
				return;
			}

			for (Map.Entry<Serializable, Object> entry : kafka.<Serializable, Object> seek(keyspace.toString(), 0)) {
				accessor.put(entry.getKey(), entry.getValue(), keyspace);
			}
		}

		private void publishToKafka(Serializable keyspace, Serializable id, Object item) {
			kafka.send(keyspace.toString(), id, item);
		}

	}

	interface KafkaOperations {

		void send(String topic, Serializable key, Object value);

		<K, V> Iterable<Map.Entry<K, V>> seek(String topic, int partition);
	}

}
