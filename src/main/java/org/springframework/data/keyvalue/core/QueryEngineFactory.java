/*
 * Copyright 2024-2025 the original author or authors.
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

/**
 * Interface for {@code QueryEngineFactory} implementations that provide a {@link QueryEngine} object as part of the
 * configuration.
 * <p>
 * The factory is used during configuration to supply the query engine to be used. When configured, a
 * {@code QueryEngineFactory} can be instantiated by accepting a {@link SortAccessor} in its constructor. Otherwise,
 * implementations are expected to declare a no-args constructor.
 *
 * @author Mark Paluch
 * @since 3.3.1
 */
public interface QueryEngineFactory {

	/**
	 * Factory method for creating a {@link QueryEngine}.
	 *
	 * @return the query engine.
	 */
	QueryEngine<?, ?, ?> create();
}
