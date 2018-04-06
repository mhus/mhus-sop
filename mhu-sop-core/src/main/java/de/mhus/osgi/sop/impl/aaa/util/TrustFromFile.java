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
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;
import de.mhus.osgi.sop.api.util.SopUtil;

public class TrustFromFile extends MLog implements TrustSource {
	
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

}
