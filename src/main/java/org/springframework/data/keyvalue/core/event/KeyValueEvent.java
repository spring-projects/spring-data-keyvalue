/*
 * Copyright 2015-2021 the original author or authors.
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
package org.springframework.data.keyvalue.core.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

/**
 * {@link KeyValueEvent} gets published for operations executed by eg.
 * {@link org.springframework.data.keyvalue.core.KeyValueTemplate}.
 *
 * Use the {@link KeyValueEvent.KeyBasedEvent#getType()} to determine which event has been emitted.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
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
	 * @param keyspace
	 * @param type
	 * @return
	 */
	public static <T> BeforeGetEvent<T> beforeGet(Object id, String keyspace, Class<T> type) {
		return new BeforeGetEvent<>(id, keyspace, type);
	}

	/**
	 * Create new {@link AfterGetEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterGetEvent<T> afterGet(Object id, String keyspace, Class<T> type, @Nullable T value) {
		return new AfterGetEvent<>(id, keyspace, type, value);
	}

	/**
	 * Create new {@link BeforeInsertEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> BeforeInsertEvent<T> beforeInsert(Object id, String keyspace, Class<? extends T> type, T value) {
		return new BeforeInsertEvent<>(id, keyspace, type, value);
	}

	/**
	 * Create new {@link AfterInsertEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterInsertEvent<T> afterInsert(Object id, String keyspace, Class<? extends T> type, T value) {
		return new AfterInsertEvent<>(id, keyspace, type, value);
	}

	/**
	 * Create new {@link BeforeUpdateEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> BeforeUpdateEvent<T> beforeUpdate(Object id, String keyspace, Class<? extends T> type, T value) {
		return new BeforeUpdateEvent<>(id, keyspace, type, value);
	}

	/**
	 * Create new {@link AfterUpdateEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param actualValue
	 * @param previousValue
	 * @return
	 */
	public static <T> AfterUpdateEvent<T> afterUpdate(Object id, String keyspace, Class<? extends T> type, T actualValue,
			@Nullable Object previousValue) {
		return new AfterUpdateEvent<>(id, keyspace, type, actualValue, previousValue);
	}

	/**
	 * Create new {@link BeforeDropKeySpaceEvent}.
	 *
	 * @param keyspace
	 * @param type
	 * @return
	 */
	public static <T> BeforeDropKeySpaceEvent<T> beforeDropKeySpace(String keyspace, Class<? extends T> type) {
		return new BeforeDropKeySpaceEvent<>(keyspace, type);
	}

	/**
	 * Create new {@link AfterDropKeySpaceEvent}.
	 *
	 * @param keyspace
	 * @param type
	 * @return
	 */
	public static <T> AfterDropKeySpaceEvent<T> afterDropKeySpace(String keyspace, Class<? extends T> type) {
		return new AfterDropKeySpaceEvent<>(keyspace, type);
	}

	/**
	 * Create new {@link BeforeDeleteEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @return
	 */
	public static <T> BeforeDeleteEvent<T> beforeDelete(Object id, String keyspace, Class<? extends T> type) {
		return new BeforeDeleteEvent<>(id, keyspace, type);
	}

	/**
	 * Create new {@link AfterDeleteEvent}.
	 *
	 * @param id
	 * @param keyspace
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T> AfterDeleteEvent<T> afterDelete(Object id, String keyspace, Class<? extends T> type,
			@Nullable T value) {
		return new AfterDeleteEvent<>(id, keyspace, type, value);
	}

	/**
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	abstract static class KeyBasedEvent<T> extends KeyValueEvent<T> {

		private Object key;
		private Class<? extends T> type;

		KeyBasedEvent(Object key, String keyspace, Class<? extends T> type) {

			super(type, keyspace);
			this.key = key;
			this.type = type;
		}

		public Object getKey() {
			return key;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.EventObject#getSource()
		 */
		@Override
		public Object getSource() {
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

		private final @Nullable T payload;

		KeyBasedEventWithPayload(Object key, String keyspace, Class<? extends T> type, @Nullable T payload) {
			super(key, keyspace, type);
			this.payload = payload;
		}

		/**
		 * Get the value of the element the {@link KeyValueEvent} refers to. Can be {@literal null}.
		 *
		 * @return
		 */
		@Nullable
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

		protected BeforeGetEvent(Object key, String keyspace, Class<T> type) {
			super(key, keyspace, type);
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

		protected AfterGetEvent(Object key, String keyspace, Class<T> type, @Nullable T payload) {
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

		public BeforeInsertEvent(Object key, String keyspace, Class<? extends T> type, @Nullable T payload) {
			super(key, keyspace, type, payload);

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

		public AfterInsertEvent(Object key, String keyspace, Class<? extends T> type, @Nullable T payload) {
			super(key, keyspace, type, payload);
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

		public BeforeUpdateEvent(Object key, String keyspace, Class<? extends T> type, @Nullable T payload) {
			super(key, keyspace, type, payload);
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

		private final @Nullable Object existing;

		public AfterUpdateEvent(Object key, String keyspace, Class<? extends T> type, T payload,
				@Nullable Object existing) {
			super(key, keyspace, type, payload);
			this.existing = existing;
		}

		/**
		 * Get the value before update. Can be {@literal null}.
		 *
		 * @return
		 */
		@Nullable
		public Object before() {
			return existing;
		}

		/**
		 * Get the current value.
		 *
		 * @return can be {@literal null}.
		 */
		@Nullable
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

		public BeforeDeleteEvent(Object key, String keyspace, Class<? extends T> type) {
			super(key, keyspace, type);
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

		public AfterDeleteEvent(Object key, String keyspace, Class<? extends T> type, @Nullable T payload) {
			super(key, keyspace, type, payload);
		}
	}

	/**
	 * {@link KeyValueEvent} before removing all elements in a given {@literal keyspace}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class BeforeDropKeySpaceEvent<T> extends KeyValueEvent<T> {

		public BeforeDropKeySpaceEvent(String keyspace, Class<? extends T> type) {
			super(type, keyspace);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<T> getSource() {
			return (Class<T>) super.getSource();
		}

	}

	/**
	 * {@link KeyValueEvent} after removing all elements in a given {@literal keyspace}.
	 *
	 * @author Christoph Strobl
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static class AfterDropKeySpaceEvent<T> extends KeyValueEvent<T> {

		public AfterDropKeySpaceEvent(String keyspace, Class<? extends T> type) {
			super(type, keyspace);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<T> getSource() {
			return (Class<T>) super.getSource();
		}
	}
}
