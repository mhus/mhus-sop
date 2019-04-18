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
package de.mhus.osgi.sop.api.util;

import java.util.List;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.M;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.SopObjectParameter;

public class ObjectUtil {
	
	public static SopObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return M.l(AdbApi.class).getParameter(type, id, key);
	}

	public static void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		M.l(AdbApi.class).setParameter(type, id, key, value);
	}

	public static void deleteAll(Class<?> type, UUID id) throws MException {
		M.l(AdbApi.class).deleteParameters(type, id);
	}

	public static List<SopObjectParameter> getParameters(Class<?> type, String key, String value) throws MException {
		return M.l(AdbApi.class).getParameters(type, key, value);
	}

	public static String getRecursiveValue(Class<? extends DbMetadata> clazz, UUID id, String key, String def) throws MException {
		DbMetadata obj = M.l(AdbApi.class).getObject(clazz, id);
		return getRecursiveValue(obj, key, def);
	}
	
	public static String getRecursiveValue(DbMetadata obj, String key, String def) {
		SopObjectParameter out = null;
		try {
			out = M.l(AdbApi.class).getRecursiveParameter(obj, key);
		} catch (MException e) {
			
		}
		 if (out == null || out.getValue() == null) return def;
		 return out.getValue();
	}	

}
