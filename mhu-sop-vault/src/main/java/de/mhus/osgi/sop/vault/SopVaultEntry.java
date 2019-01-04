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
package de.mhus.osgi.sop.vault;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.util.AdbUtil;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.core.vault.VaultEntry;
import de.mhus.lib.errors.MException;

public class SopVaultEntry extends DbMetadata implements VaultEntry {

	@DbPersistent
	protected String type;
	@DbPersistent(type=TYPE.BLOB)
	protected String description;
	@DbPersistent
	protected SecureString value;

	public SopVaultEntry() {}
	
	public SopVaultEntry(VaultEntry clone) {
		AdbUtil.setId(this, clone.getId());
		type = clone.getType();
		description = clone.getDescription();
		value = clone.getValue();
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public SecureString getValue() {
		return value;
	}

}
