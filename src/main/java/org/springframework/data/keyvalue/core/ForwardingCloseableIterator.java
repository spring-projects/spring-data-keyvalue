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
package org.springframework.data.keyvalue.core;

import java.util.Iterator;
import java.util.Map;

import org.springframework.data.util.CloseableIterator;

/**
 * Forwards {@link CloseableIterator} invocations to the configured {@link Iterator} delegate.
 * 
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @param <K>
 * @param <V>
 */
public class ForwardingCloseableIterator<K, V> implements CloseableIterator<Map.Entry<K, V>> {

	private final Iterator<? extends Map.Entry<K, V>> delegate;
	private final Runnable closeHandler;

	/**
	 * Creates a new {@link ForwardingCloseableIterator}.
	 * 
	 * @param delegate must not be {@literal null}
	 */
	public ForwardingCloseableIterator(Iterator<? extends Map.Entry<K, V>> delegate) {
		this(delegate, null);
	}

	/**
	 * Creates a new {@link ForwardingCloseableIterator} that invokes the configured {@code closeHanlder} on {@link #close()}.
	 * 
	 * @param delegate must not be {@literal null}
	 * @param closeHandler may be {@literal null}
	 */
	public ForwardingCloseableIterator(Iterator<? extends Map.Entry<K, V>> delegate, Runnable closeHandler) {
		this.delegate = delegate;
		this.closeHandler = closeHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Map.Entry<K, V> next() {
		return delegate.next();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.util.CloseableIterator#close()
	 */
	@Override
	public void close() {
		if (closeHandler != null) {
			closeHandler.run();
		}
	}
}
