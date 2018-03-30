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

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.basics.Ace;
import de.mhus.lib.basics.AclControlled;
import de.mhus.lib.errors.MException;

public class SopAcl extends DbMetadata implements AclControlled {
	
	@DbPersistent(type=TYPE.BLOB)
	private String list;
	@DbPersistent
	@DbIndex("u1")
	private String target;
	
	public SopAcl() {}
	
	public SopAcl(String target, String list) {
		this.target = target;
		this.list = list;
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	@Override
	public String getAcl() {
		return "*=" + Ace.RIGHTS_RO;
	}

	public String getList() {
		return list;
	}

	public String getTarget() {
		return target;
	}

	public void setList(String list) {
		this.list = list;
	}

}
