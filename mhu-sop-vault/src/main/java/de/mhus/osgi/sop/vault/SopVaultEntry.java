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
