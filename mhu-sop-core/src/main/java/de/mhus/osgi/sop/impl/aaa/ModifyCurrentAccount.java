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
