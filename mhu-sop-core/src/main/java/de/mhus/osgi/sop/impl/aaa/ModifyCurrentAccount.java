package de.mhus.osgi.sop.impl.aaa;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.ModifyCurrentAccountApi;
import de.mhus.lib.errors.MException;

public class ModifyCurrentAccount implements ModifyCurrentAccountApi {

	private Account account;
	private AccountSource source;

	public ModifyCurrentAccount(Account current, AccountSource accountSource) {
		this.account = current;
		this.source = accountSource;
	}

	@Override
	public Account getAccount() {
		return account;
	}

	@Override
	public void changePassword(String newPassword) throws MException {
		source.getModifyApi().changePassword(account.getName(), newPassword);
	}

	@Override
	public void changeAccount(IReadProperties properties) throws MException {
		source.getModifyApi().changeAccount(account.getName(), properties);
	}

}
