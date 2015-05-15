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
package org.springframework.data.keyvalue;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.util.ObjectUtils;

/**
 * A {@link Persistent} type with {@link CustomKeySpaceAnnotation}.
 * 
 * @author Christoph Strobl
 */
@CustomKeySpaceAnnotation(name = "aliased")
public class TypeWithCustomComposedKeySpaceAnnotation {

	@Id String id;
	String name;

	public TypeWithCustomComposedKeySpaceAnnotation(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ObjectUtils.nullSafeHashCode(this.id);
		result = prime * result + ObjectUtils.nullSafeHashCode(this.name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TypeWithCustomComposedKeySpaceAnnotation)) {
			return false;
		}
		TypeWithCustomComposedKeySpaceAnnotation other = (TypeWithCustomComposedKeySpaceAnnotation) obj;
		if (!ObjectUtils.nullSafeEquals(this.id, other.id)) {
			return false;
		}
		if (!ObjectUtils.nullSafeEquals(this.name, other.name)) {
			return false;
		}
		return true;
	}

}
