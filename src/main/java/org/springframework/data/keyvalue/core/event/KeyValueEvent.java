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
package org.springframework.data.keyvalue.core.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;

/**
 * {@link KeyValueEvent} gets published for operations executed by eg.
 * {@link org.springframework.data.keyvalue.core.KeyValueTemplate}. Use the {@link #getType()} to determine which event
 * has been emitted.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @param <T>
 */
public class KeyValueEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = -7128527253428193044L;

	private final String keyspace;

	protected KeyValueEvent(Object source, String keyspace) {

		super(source);
		this.keyspace = keyspace;
	}

	/**
	 * @return affected keyspace. Never {@literal null}.
	 */
	public String getKeyspace() {
		return keyspace;
	}

	@Override
	public String toString() {
		return "KeyValueEvent [keyspace=" + keyspace + ", source=" + getSource() + "]";
	}

	/**
	 * Create new {@link BeforeGetEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @return
	 */
	public static <T> BeforeGetEvent<T> beforeGet(Serializable id, String keySpace, Class<T> type) {
		return new BeforeGetEvent<>(id, keySpace, type);
	}

	/**
	 * Create new {@link AfterGetEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterGetEvent<T> afterGet(Serializable id, String keySpace, Class<T> type, T value) {
		return new AfterGetEvent<>(id, keySpace, type, value);
	}

	/**
	 * Create new {@link BeforeInsertEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> BeforeInsertEvent<T> beforeInsert(Serializable id, String keySpace, Class<? extends T> type, T value) {
		return new BeforeInsertEvent<>(id, keySpace, type, value);
	}

	/**
	 * Create new {@link AfterInsertEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterInsertEvent<T> afterInsert(Serializable id, String keySpace, Class<? extends T> type, T value) {
		return new AfterInsertEvent<>(id, keySpace, type, value);
	}

	/**
	 * Create new {@link BeforeUpdateEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> BeforeUpdateEvent<T> beforeUpdate(Serializable id, String keySpace, Class<? extends T> type, T value) {
		return new BeforeUpdateEvent<>(id, keySpace, type, value);
	}

	/**
	 * Create new {@link AfterUpdateEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param actualValue
	 * @param previousValue
	 * @return
	 */
	public static <T> AfterUpdateEvent<T> afterUpdate(Serializable id, String keySpace, Class<? extends T> type,
			T actualValue, Object previousValue) {
		return new AfterUpdateEvent<>(id, keySpace, type, actualValue, previousValue);
	}

	/**
	 * Create new {@link BeforeDropKeySpaceEvent}.
	 *
	 * @param keySpace
	 * @param type
	 * @return
	 */
	public static <T> BeforeDropKeySpaceEvent<T> beforeDropKeySpace(String keySpace, Class<? extends T> type) {
		return new BeforeDropKeySpaceEvent<>(keySpace, type);
	}

	/**
	 * Create new {@link AfterDropKeySpaceEvent}.
	 *
	 * @param keySpace
	 * @param type
	 * @return
	 */
	public static <T> AfterDropKeySpaceEvent<T> afterDropKeySpace(String keySpace, Class<? extends T> type) {
		return new AfterDropKeySpaceEvent<>(keySpace, type);
	}

	/**
	 * Create new {@link BeforeDeleteEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @return
	 */
	public static <T> BeforeDeleteEvent<T> beforeDelete(Serializable id, String keySpace, Class<? extends T> type) {
		return new BeforeDeleteEvent<>(id, keySpace, type);
	}

	/**
	 * Create new {@link AfterDeleteEvent}.
	 *
	 * @param id
	 * @param keySpace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterDeleteEvent<T> afterDelete(Serializable id, String keySpace, Class<? extends T> type, T value) {
		return new AfterDeleteEvent<>(id, keySpace, type, value);
	}

	/**
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	abstract static class KeyBasedEvent<T> extends KeyValueEvent<T> {

		private Serializable key;
		private Class<? extends T> type;

		protected KeyBasedEvent(Serializable key, String keySpace, Class<? extends T> type) {

			super(type, keySpace);
			this.key = key;
		}

		public Serializable getKey() {
			return key;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.EventObject#getSource()
		 */
		@Override
		public Serializable getSource() {
			return getKey();
		}

		/**
		 * Get the type of the element the {@link KeyValueEvent} refers to.
		 *
		 * @return
		 */
		public Class<? extends T> getType() {
			return type;
		}
	}

	/**
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	abstract static class KeyBasedEventWithPayload<T> extends KeyBasedEvent<T> {

		private final T payload;

		public KeyBasedEventWithPayload(Serializable key, String keySpace, Class<? extends T> type, T payload) {
			super(key, keySpace, type);
			this.payload = payload;
		}

		/**
		 * Get the value of the element the {@link KeyValueEvent} refers to. Can be {@literal null}.
		 *
		 * @return
		 */
		public T getPayload() {
			return payload;
		}
	}

	/**
	 * {@link KeyValueEvent} raised before loading an object by its {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeGetEvent<T> extends KeyBasedEvent<T> {

		protected BeforeGetEvent(Serializable key, String keySpace, Class<T> type) {
			super(key, keySpace, type);
		}

	}

	/**
	 * {@link KeyValueEvent} after loading an object by its {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterGetEvent<T> extends KeyBasedEventWithPayload<T> {

		protected AfterGetEvent(Serializable key, String keyspace, Class<T> type, T payload) {
			super(key, keyspace, type, payload);
		}

	}

	/**
	 * {@link KeyValueEvent} before inserting an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeInsertEvent<T> extends KeyBasedEventWithPayload<T> {

		public BeforeInsertEvent(Serializable key, String keySpace, Class<? extends T> type, T payload) {
			super(key, keySpace, type, payload);

		}
	}

	/**
	 * {@link KeyValueEvent} after inserting an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterInsertEvent<T> extends KeyBasedEventWithPayload<T> {

		public AfterInsertEvent(Serializable key, String keySpace, Class<? extends T> type, T payload) {
			super(key, keySpace, type, payload);
		}
	}

	/**
	 * {@link KeyValueEvent} before updating an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeUpdateEvent<T> extends KeyBasedEventWithPayload<T> {

		public BeforeUpdateEvent(Serializable key, String keySpace, Class<? extends T> type, T payload) {
			super(key, keySpace, type, payload);
		}
	}

	/**
	 * {@link KeyValueEvent} after updating an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterUpdateEvent<T> extends KeyBasedEventWithPayload<T> {

		private final Object existing;

		public AfterUpdateEvent(Serializable key, String keySpace, Class<? extends T> type, T payload, Object existing) {
			super(key, keySpace, type, payload);
			this.existing = existing;
		}

		/**
		 * Get the value before update. Can be {@literal null}.
		 *
		 * @return
		 */
		public Object before() {
			return existing;
		}

		/**
		 * Get the current value.
		 *
		 * @return
		 */
		public T after() {
			return getPayload();
		}
	}

	/**
	 * {@link KeyValueEvent} before removing an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeDeleteEvent<T> extends KeyBasedEvent<T> {

		public BeforeDeleteEvent(Serializable key, String keySpace, Class<? extends T> type) {
			super(key, keySpace, type);
		}
	}

	/**
	 * {@link KeyValueEvent} after removing an object by with a given {@literal key}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterDeleteEvent<T> extends KeyBasedEventWithPayload<T> {

		public AfterDeleteEvent(Serializable key, String keySpace, Class<? extends T> type, T payload) {
			super(key, keySpace, type, payload);
		}
	}

	/**
	 * {@link KeyValueEvent} before removing all elements in a given {@literal keySpace}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeDropKeySpaceEvent<T> extends KeyValueEvent<T> {

		public BeforeDropKeySpaceEvent(String keySpace, Class<? extends T> type) {
			super(type, keySpace);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<T> getSource() {
			return (Class<T>) super.getSource();
		}

	}

	/**
	 * {@link KeyValueEvent} after removing all elements in a given {@literal keySpace}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterDropKeySpaceEvent<T> extends KeyValueEvent<T> {

		public AfterDropKeySpaceEvent(String keySpace, Class<? extends T> type) {
			super(type, keySpace);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<T> getSource() {
			return (Class<T>) super.getSource();
		}
	}
}
