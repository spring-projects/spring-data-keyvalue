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
public class KeyValueEvent extends ApplicationEvent {

	private static final long serialVersionUID = -7128527253428193044L;

	public enum Type {
		ANY, BEFORE_INSERT, AFTER_INSERT, BEFORE_UPDATE, AFTER_UPDATE, BEFORE_DELETE, AFTER_DELETE, BEFORE_GET, AFTER_GET
	}

	private final Type type;
	private final String keyspace;
	private final Serializable id;
	private final Object value;

	protected KeyValueEvent(Object source, Type type, String keyspace, Serializable id, Object value) {
		
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

	@Override
	public String toString() {
		return "KeyValueEvent [type=" + type + ", keyspace=" + keyspace + ", id=" + id + "]";
	}

	public static GetEvent beforeGet(Object source, String keyspace, Serializable id) {
		return new GetEvent(source, Type.BEFORE_GET, keyspace, id, null);
	}

	public static GetEvent afterGet(Object source, String keyspace, Serializable id, Object value) {
		return new GetEvent(source, Type.AFTER_GET, keyspace, id, value);
	}

	public static InsertEvent beforeInsert(Object source, String keyspace, Serializable id, Object value) {
		return new InsertEvent(source, Type.BEFORE_INSERT, keyspace, id, value);
	}

	public static InsertEvent afterInsert(Object source, String keyspace, Serializable id, Object value) {
		return new InsertEvent(source, Type.AFTER_INSERT, keyspace, id, value);
	}

	public static UpdateEvent beforeUpdate(Object source, String keyspace, Serializable id, Object value) {
		return new UpdateEvent(source, Type.BEFORE_UPDATE, keyspace, id, value);
	}

	public static UpdateEvent afterUpdate(Object source, String keyspace, Serializable id, Object value) {
		return new UpdateEvent(source, Type.AFTER_UPDATE, keyspace, id, value);
	}

	public static DropKeyspaceEvent beforeDelete(Object source, String keyspace) {
		return new DropKeyspaceEvent(source, Type.BEFORE_DELETE, keyspace);
	}

	public static DeleteEvent beforeDelete(Object source, String keyspace, Serializable id) {
		return beforeDelete(source, keyspace, id, null);
	}

	public static DeleteEvent beforeDelete(Object source, String keyspace, Serializable id, Object value) {
		return new DeleteEvent(source, Type.BEFORE_DELETE, keyspace, id, value);
	}

	public static DropKeyspaceEvent afterDelete(Object source, String keyspace) {
		return new DropKeyspaceEvent(source, Type.AFTER_DELETE, keyspace);
	}

	public static DeleteEvent afterDelete(Object source, String keyspace, Serializable id, Object value) {
		return new DeleteEvent(source, Type.AFTER_DELETE, keyspace, id, value);
	}

	public static class InsertEvent extends KeyValueEvent {

		private static final long serialVersionUID = -1;

		InsertEvent(Object source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class UpdateEvent extends KeyValueEvent {

		private static final long serialVersionUID = -1;

		UpdateEvent(Object source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class DeleteEvent extends KeyValueEvent {

		private static final long serialVersionUID = -1;

		DeleteEvent(Object source, Type type, String keyspace, Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}
	}

	public static class DropKeyspaceEvent extends DeleteEvent {

		private static final long serialVersionUID = -1;

		DropKeyspaceEvent(Object source, Type type, String keyspace) {
			super(source, type, keyspace, null, null);
		}
	}

	public static class GetEvent extends KeyValueEvent {

		private static final long serialVersionUID = -1;

		protected GetEvent(Object source, Type type, String keyspace,
				Serializable id, Object value) {
			super(source, type, keyspace, id, value);
		}

	}

}
