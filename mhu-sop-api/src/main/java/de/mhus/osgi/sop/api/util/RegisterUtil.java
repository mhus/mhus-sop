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
import java.util.Map;

import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.M;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopRegister;

public class RegisterUtil {

	private static final Log log = Log.getLog(RegisterUtil.class);
	
	public static boolean isEmpty(Class<?> clazz) throws MException {
		return isEmpty(clazz.getCanonicalName());
	}
	
	public static boolean isEmpty(String name) throws MException {
		long cnt = M.l(SopApi.class).getManager().count(Db.query(SopRegister.class).eq("name", name));
		return cnt == 0;
	}
	
	public static void fillKey(Class<?> clazz, List<String> list) {
		fillKey(clazz.getCanonicalName(), list);
	}
	
	public static void fillKey(String name, List<String> list) {
		XdbService db = M.l(SopApi.class).getManager();
		if (list == null) return;
		for (String item : list) {
			try {
				db.inject(new SopRegister(name, item, null, null, null)).save();
			} catch (MException e) {
				log.d("fillKey",item,e);
			}
		}
	}
	
	public static void fillKeyValue(Class<?> clazz, Map<String,String> map) {
		fillKeyValue(clazz.getCanonicalName(), map);
	}
	
	public static void fillKeyValue(String name, Map<String,String> map) {
		XdbService db = M.l(SopApi.class).getManager();
		if (map == null) return;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			try {
				db.inject(new SopRegister(name, entry.getKey(), null, entry.getValue(), null)).save();
			} catch (MException e) {
				log.d("fillKeyValue",entry,e);
			}
		}
	}
	
	public static void fillKeyKey(Class<?> clazz, Map<String,String> map) {
		fillKeyKey(clazz.getCanonicalName(), map);
	}
	
	public static void fillKeyKey(String name, Map<String,String> map) {
		XdbService db = M.l(SopApi.class).getManager();
		if (map == null) return;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			try {
				db.inject(new SopRegister(name, entry.getKey(), entry.getValue(), null, null)).save();
			} catch (MException e) {
				log.d("fillKeyKey",entry,e);
			}
		}
	}
	
	public static boolean hasKey(Class<?> clazz, String key) throws MException {
		return hasKey(clazz.getCanonicalName(), key);
	}
	
	public static boolean hasKey(String name, String key) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key == null) return false;
		SopRegister out = db.getObjectByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key));
		return out != null;
	}
	
	public static SopRegister getByKey(Class<?> clazz, String key) throws MException {
		return getByKey(clazz.getCanonicalName(), key);
	}
	
	public static SopRegister getByKey(String name, String key) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key == null) return null;
		SopRegister out = db.getObjectByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key));
		return out;
	}
	
	public static boolean hasKeyKey(Class<?> clazz, String key1, String key2) throws MException {
		return hasKeyKey(clazz.getCanonicalName(), key1, key2);
	}
	
	public static boolean hasKeyKey(String name, String key1, String key2) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key1 == null || key2 == null) return false;
		SopRegister out = db.getObjectByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key1).eq("key2", key2));
		return out != null;
	}
	
	public static SopRegister getByKeyKey(Class<?> clazz, String key1, String key2) throws MException {
		return getByKeyKey(clazz.getCanonicalName(), key1, key2);
	}
	
	public static SopRegister getByKeyKey(String name, String key1, String key2) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key1 == null || key2 == null) return null;
		SopRegister out = db.getObjectByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key1).eq("key2", key2));
		return out;
	}

	public static List<SopRegister> listByKey(Class<?> clazz, String key) throws MException {
		return listByKey(clazz.getCanonicalName(), key);
	}
	
	public static List<SopRegister> listByKey(String name, String key) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key == null) return null;
		return db.getByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key)).toCacheAndClose();
	}

	public static List<SopRegister> listByKeyKey(Class<?> clazz, String key1, String key2) throws MException {
		return listByKeyKey(clazz.getCanonicalName(), key1, key2);
	}
	
	public static List<SopRegister> listByKeyKey(String name, String key1, String key2) throws MException {
		XdbService db = M.l(SopApi.class).getManager();
		if (key1 == null || key2 == null) return null;
		return db.getByQualification(Db.query(SopRegister.class).eq("name", name).eq("key1", key1).eq("key2", key2)).toCacheAndClose();
	}

	public static SopRegister set(Class<?> clazz, String key1, String key2, String value1, String value2) throws MException {
		return set(clazz.getCanonicalName(), key1, key2, value1, value2);
	}

	public static SopRegister set(String name, String key1, String key2, String value1, String value2) throws MException {

		SopRegister r = getByKeyKey(name, key1, key2);
		if (r == null) {
			XdbService db = M.l(SopApi.class).getManager();
			r = db.inject(new SopRegister(name, key1, key2, null, null));
		}
		r.setValue1(value1);
		r.setValue2(value2);
		
		r.save();
		return r;
	}
	
}
