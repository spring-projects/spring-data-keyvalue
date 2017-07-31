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
package org.springframework.data.keyvalue;

import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * Class that inherits its {@link KeySpace} from a super class annotated with a custom {@link CustomKeySpaceAnnotation}
 * annotation.
 * 
 * @author Christoph Strobl
 */
public class SubclassOfTypeWithCustomComposedKeySpaceAnnotation
		extends TypeWithCustomComposedKeySpaceAnnotationUsingAliasFor {

	public SubclassOfTypeWithCustomComposedKeySpaceAnnotation(String name) {
		super(name);
	}

}
