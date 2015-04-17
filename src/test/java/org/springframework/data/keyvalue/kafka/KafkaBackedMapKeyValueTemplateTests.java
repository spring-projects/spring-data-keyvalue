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
package org.springframework.data.keyvalue.kafka;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueCallback;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.event.KeyValueEvent;
import org.springframework.data.keyvalue.core.event.KeyValueEvent.InsertEvent;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Just a simple POC to explore interaction with kafka to potentially back an in memory map implementation with a remote
 * log. <br />
 * Assumption only one consumer / producer per topic.
 * 
 * @author Christoph Strobl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaBackedMapKeyValueTemplateTests {

	@Configuration
	@ComponentScan
	static class Config {

		@Bean
		KeyValueTemplate keyValueTemplate() {
			KeyValueTemplate template = new KeyValueTemplate(new MapKeyValueAdapter());
			template.setEventTypesToPublish(Collections.singleton(KeyValueEvent.Type.ANY));
			return template;
		}

		@Bean
		KafkaProducer<Serializable, Serializable> kafkaProducer() {

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			props.put("bootstrap.servers", "localhost:9092");

			KafkaProducer<Serializable, Serializable> producer = new KafkaProducer<Serializable, Serializable>(props);
			return producer;
		}

		@Bean
		KafkaConsumer<Serializable, Serializable> kafkaConsumer() {

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			props.put("bootstrap.servers", "localhost:9092");
			props.put("group.id", "foobar");
			props.put("partition.assignment.strategy", "range");

			KafkaConsumer<Serializable, Serializable> consumer = new KafkaConsumer<Serializable, Serializable>(props);
			return consumer;
		}

	}

	@Autowired KeyValueTemplate template;

	@Test
	public void loadFromKafkaAtFirstEventThenPublishThere() {

		assertThat(template.findById("foo", String.class), is("bar"));

		template.insert("1", "one");

		assertThat(template.findById("foo", String.class), is("bar"));
		assertThat(template.findById("1", String.class), is("one"));
		assertThat(template.count(String.class), is(2L));
	}

	@Component
	public static class ProducingKafkaEventListener implements ApplicationListener<InsertEvent<?>> {

		@Autowired KafkaProducer<Serializable, Serializable> producer;

		@Override
		public void onApplicationEvent(InsertEvent<?> event) {

			producer.send(new ProducerRecord<Serializable, Serializable>(event.getKeyspace(), event.getId(),
					(Serializable) event.getValue()));
		}
	}

	@Component
	public static class ConsumingKafkaEventListener implements ApplicationListener<KeyValueEvent<KeyValueTemplate>> {

		@Autowired KafkaConsumer<Serializable, Serializable> consumer;
		private Set<Serializable> knownTopics = new HashSet<Serializable>();

		@Override
		public void onApplicationEvent(final KeyValueEvent<KeyValueTemplate> event) {

			if (!knownTopics.contains(event.getKeyspace())) {

				knownTopics.add(event.getKeyspace());

				if (!event.getSource().execute(new KeyValueCallback<Boolean>() {

					@Override
					public Boolean doInKeyValue(KeyValueAdapter adapter) {
						return adapter.hasKeyspace(event.getKeyspace());
					}
				})) {

					// read stuff from kafka
					consumer.seek(Collections.singletonMap(new TopicPartition(event.getKeyspace(), 0), 0L));

					// Thank you kafka for just returning null at this place -> an error would have been nice instead of
					// swallowing stuff without a word of warning.
					final Map<String, ConsumerRecords<Serializable, Serializable>> payload = consumer.poll(10);

					// directly inject it into the store
					event.getSource().execute(new KeyValueCallback<Void>() {

						@Override
						public Void doInKeyValue(KeyValueAdapter adapter) {

							if (payload != null) {
								// TODO process data and add to the adapter
							}

							// since poll is not yet implemented we'll just add some stuff manually here
							adapter.put("foo", "bar", event.getKeyspace());
							return null;
						}
					});
				}
			}
		}
	}
}
