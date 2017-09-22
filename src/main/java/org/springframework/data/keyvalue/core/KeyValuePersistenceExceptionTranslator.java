/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.NoSuchElementException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simple {@link PersistenceExceptionTranslator} implementation for key/value stores that converts the given runtime
 * exception to an appropriate exception from the {@code org.springframework.dao} hierarchy.
 * 
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class KeyValuePersistenceExceptionTranslator implements PersistenceExceptionTranslator {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.dao.support.PersistenceExceptionTranslator#translateExceptionIfPossible(java.lang.RuntimeException)
	 */
	@Nullable
	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException exception) {

		Assert.notNull(exception, "Exception must not be null!");

		if (exception instanceof DataAccessException) {
			return (DataAccessException) exception;
		}

		if (exception instanceof NoSuchElementException || exception instanceof IndexOutOfBoundsException
				|| exception instanceof IllegalStateException) {
			return new DataRetrievalFailureException(exception.getMessage(), exception);
		}

		if (exception.getClass().getName().startsWith("java")) {
			return new UncategorizedKeyValueException(exception.getMessage(), exception);
		}

		return null;
	}
}
