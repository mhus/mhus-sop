package de.mhus.osgi.sop.vault;

import de.mhus.lib.core.vault.DefaultVaultSourceFactory;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.VaultPassphrase;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.lib.core.vault.VaultSourceFactory;

//@Component
@Deprecated
public class SopVaultSourceFactory extends DefaultVaultSourceFactory implements VaultSourceFactory {
	
	private SopVaultSource def;

	@Override
	public VaultSource create(String name, VaultPassphrase vaultPassphrase) {
		if (MVault.SOURCE_DEFAULT.equals(name)) {
			return getDefaultSource();
		}
		return super.create(name, vaultPassphrase);
	}

	private synchronized VaultSource getDefaultSource() {
		if (def == null)
			def = new SopVaultSource();
		return def;
	}

}
