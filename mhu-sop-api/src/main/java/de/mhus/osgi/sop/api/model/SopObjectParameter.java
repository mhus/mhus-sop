/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.api.model;

import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;

public class SopObjectParameter extends DbMetadata {

	public static final String TYPE_GLOBAL = "_global";

	@DbIndex({"1","2","3"})
	@DbPersistent
	private String objectType;
	@DbIndex({"1","2"})
	@DbPersistent
	private UUID   objectId;
	@DbIndex({"1","3"})
	@DbPersistent
	private String key;
	@DbPersistent
	@DbIndex("3")
	private String value;
	
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public UUID getObjectId() {
		return objectId;
	}
	public void setObjectId(UUID objectId) {
		this.objectId = objectId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return MSystem.toString(this,getId(),objectType,objectId,key,value);
	}
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

}
