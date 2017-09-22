/*
 * Copyright 2015-2017 the original author or authors.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.data.keyvalue.core.IterableConverter.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class IterableConverterUnitTests {

	@Test // DATAKV-101
	public void toListShouldReturnEmptyListWhenSourceEmpty() {
		assertThat(toList(Collections.emptySet()), empty());
	}

	@Test // DATAKV-101
	public void toListShouldReturnSameObjectWhenSourceIsAlreadyListType() {

		List<String> source = new ArrayList<>();

		assertThat(toList(source), sameInstance(source));
	}

	@Test // DATAKV-101
	public void toListShouldReturnListWhenSourceIsNonListType() {

		Set<String> source = new HashSet<>();
		source.add("tyrion");

		assertThat(toList(source), instanceOf(List.class));
	}

	@Test // DATAKV-101
	public void toListShouldHoldValuesInOrderOfSource() {

		Set<String> source = new LinkedHashSet<>();
		source.add("tyrion");
		source.add("jaime");

		assertThat(toList(source), contains(source.toArray(new String[2])));
	}

}
