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
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;
import de.mhus.osgi.sop.api.util.SopUtil;
import de.mhus.osgi.sop.api.util.TicketUtil;

public class TrustFromFile extends MLog implements TrustSource {

	private HashMap<String, String> trustSecrets = new HashMap<>();
	
	@Override
	public Trust findTrust(String trust) {
		File file = SopUtil.getFile( "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml" );
		if (!file.exists() || !file.isFile()) return null;
		
		try {
			return new TrustFile(file, trust);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log().w(trust, e);
			return null;
		}
	}

	@Override
	public String createTrustTicket(String name, AaaContext user) {
		if (user == null) return null;
		String sec = null;
		synchronized (trustSecrets) {
			sec = trustSecrets.get(name);
			if (sec == null) {
				Trust trust = findTrust(name);
				if (trust == null) return null;
				sec = trust.encodeWithPassword();
				trustSecrets.put(name, sec);
			}
		}
		return TicketUtil.TRUST + TicketUtil.SEP 
				+ name + TicketUtil.SEP 
				+ sec + TicketUtil.SEP 
				+ user.getAccountId() + TicketUtil.SEP 
				+ (user.isAdminMode() ? TicketUtil.ADMIN : "");
	}

}
