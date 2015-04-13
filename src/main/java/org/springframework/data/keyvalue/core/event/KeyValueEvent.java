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
 * @param <T>
 */
public class KeyValueEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = -7128527253428193044L;

	public enum Type {
		BEFORE_INSERT, AFTER_INSERT, BEFORE_UPDATE, AFTER_UPDATE, BEFORE_DELETE, AFTER_DELETE
	}

	private final Type type;
	private final String keyspace;
	private final Serializable id;
	private final Object value;

	protected KeyValueEvent(T source, Type type, String keyspace, Serializable id, Object value) {
		super(source);
		this.type = type;
		this.keyspace = keyspace;
		this.id = id;
		this.value = value;
	}

	/**
	 * @return {@link Type} of event. Never {@literal null}.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return affected keyspace. Never {@literal null}.
	 */
	public String getKeyspace() {
		return keyspace;
	}

	/**
	 * @return can be {@literal null}.
	 */
	public Serializable getId() {
		return id;
	}

	/**
	 * @return can be {@literal null}.
	 */
	public Object getValue() {
		return value;
	}

	public static <T> InsertEvent<T> beforeInsert(T source, String keyspace, Serializable id, Object value) {
		return new InsertEvent<T>(source, Type.BEFORE_INSERT, keyspace, id, value);
	}

	public static <T> InsertEvent<T> afterInsert(T source, String keyspace, Serializable id, Object value) {
		return new InsertEvent<T>(source, Type.AFTER_INSERT, keyspace, id, value);
	}

	public static <T> UpdateEvent<T> beforeUpdate(T source, String keyspace, Serializable id, Object value) {
		return new UpdateEvent<T>(source, Type.BEFORE_UPDATE, keyspace, id, value);
	}

	public static <T> UpdateEvent<T> afterUpdate(T source, String keyspace, Serializable id, Object value) {
		return new UpdateEvent<T>(source, Type.AFTER_UPDATE, keyspace, id, value);
	}

	public static <T> DropKeyspaceEvent<T> beforeDelete(T source, String keyspace) {
		return new DropKeyspaceEvent<T>(source, Type.BEFORE_DELETE, keyspace);
	}

	public static <T> DeleteEvent<T> beforeDelete(T source, String keyspace, Serializable id) {
		return beforeDelete(source, keyspace, id, null);
	}

	public static <T> DeleteEvent<T> beforeDelete(T source, String keyspace, Serializable id, Object value) {
		return new DeleteEvent<T>(source, Type.BEFORE_DELETE, keyspace, id, value);
	}

	public static <T> DropKeyspaceEvent<T> afterDelete(T source, String keyspace) {
		return new DropKeyspaceEvent<T>(source, Type.AFTER_DELETE, keyspace);
	}

	public static <T> DeleteEvent<T> afterDelete(T source, String keyspace, Serializable id, Object value) {
		return new DeleteEvent<T>(source, Type.AFTER_DELETE, keyspace, id, value);
	}

	public static class InsertEvent<T> extends KeyValueEvent<T> {

		private static final long serialVersionUID = -1;

		InsertEvent(T source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class UpdateEvent<T> extends KeyValueEvent<T> {

		private static final long serialVersionUID = -1;

		UpdateEvent(T source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class DeleteEvent<T> extends KeyValueEvent<T> {

		private static final long serialVersionUID = -1;

		DeleteEvent(T source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class DropKeyspaceEvent<T> extends DeleteEvent<T> {

		private static final long serialVersionUID = -1;

		DropKeyspaceEvent(T source, Type type, String keyspace) {
			super(source, type, keyspace, null, null);
		}
	}

}
