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
package de.mhus.osgi.sop.impl.aaa.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.osgi.sop.api.util.SopUtil;

public class AccountFromFile extends MLog implements AccountSource {

	@Override
	public Account findAccount(String account) {
		File file = SopUtil.getFile( "aaa/account/" + MFile.normalize(account.trim()).toLowerCase() + ".xml" );
		if (!file.exists() || !file.isFile()) {
			log().i("file not found", file);
			return null;
		}
		
		try {
			return new AccountFile(file, account);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log().w(account, e);
			return null;
		}
	}

}
