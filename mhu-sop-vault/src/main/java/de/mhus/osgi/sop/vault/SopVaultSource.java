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

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.vault.MutableVaultSource;
import de.mhus.lib.core.vault.VaultEntry;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;

@Component(service=VaultSource.class)
public class SopVaultSource extends MLog implements MutableVaultSource {

	@Override
	public VaultEntry getEntry(UUID id) {
		XdbService db = M.l(SopApi.class).getManager();
		try {
			SopVaultEntry entry = db.getObject(SopVaultEntry.class, id);
			return entry;
		} catch (MException e) {
			log().d(id,e);
			return null;
		}
	}

	@Override
	public Iterable<UUID> getEntryIds() {
		XdbService db = M.l(SopApi.class).getManager();
		HashSet<UUID> out = new HashSet<>();
		// TODO use a database source set for huge lists?
		try {
			for (SopVaultEntry entry : db.getType(SopVaultEntry.class).getAll())
				out.add(entry.getId());
		} catch (MException e) {
			log().d(e);
		}
		return out;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public MutableVaultSource getEditable() {
		return this;
	}

	@Override
	public void addEntry(VaultEntry entry) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		SopVaultEntry clone = db.inject(new SopVaultEntry(entry));
		clone.save();
	}

	@Override
	public void removeEntry(UUID id) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		SopVaultEntry item = db.getObject(SopVaultEntry.class, id);
		item.delete();
	}

	@Override
	public void doLoad() throws IOException {
		
	}

	@Override
	public void doSave() throws IOException {
		
	}

	@Override
	public boolean isMemoryBased() {
		return false;
	}
	
}
