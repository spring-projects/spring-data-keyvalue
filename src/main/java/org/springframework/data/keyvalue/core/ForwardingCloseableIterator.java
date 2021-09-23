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
package org.springframework.data.keyvalue.core;

import java.util.Iterator;

import org.springframework.data.util.CloseableIterator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Forwards {@link CloseableIterator} invocations to the configured {@link Iterator} delegate.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class ForwardingCloseableIterator<T> implements CloseableIterator<T> {

	private final Iterator<? extends T> delegate;
	private final @Nullable Runnable closeHandler;

	/**
	 * Creates a new {@link ForwardingCloseableIterator}.
	 *
	 * @param delegate must not be {@literal null}.
	 */
	public ForwardingCloseableIterator(Iterator<? extends T> delegate) {
		this(delegate, null);
	}

	/**
	 * Creates a new {@link ForwardingCloseableIterator} that invokes the configured {@code closeHandler} on
	 * {@link #close()}.
	 *
	 * @param delegate must not be {@literal null}.
	 * @param closeHandler may be {@literal null}.
	 */
	public ForwardingCloseableIterator(Iterator<? extends T> delegate, @Nullable Runnable closeHandler) {

		Assert.notNull(delegate, "Delegate iterator must not be null!");

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
	public T next() {
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
