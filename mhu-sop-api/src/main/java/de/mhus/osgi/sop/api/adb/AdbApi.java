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
package de.mhus.osgi.sop.api.adb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.model.SopActionTask;
import de.mhus.osgi.sop.api.model.SopObjectParameter;

public interface AdbApi extends SApi {

	int PAGE_SIZE = 100;

	XdbService getManager();

	SopActionTask createActionTask(String queue, String action, String target, String[] properties, boolean smart) throws MException;

	List<SopActionTask> getQueue(String queue, int max) throws MException;

	List<SopObjectParameter> getParameters(Class<?> type, UUID id)
			throws MException;

	List<SopObjectParameter> getParameters(String type, UUID id) throws MException;

	void setGlobalParameter(String key, String value) throws MException;

	void setParameter(Class<?> type, UUID id, String key, String value)
			throws MException;

	void setParameter(String type, UUID id, String key, String value)
			throws MException;

	SopObjectParameter getGlobalParameter(String key) throws MException;

	String getValue(String type, UUID id, String key, String def)
			throws MException;

	String getValue(Class<?> type, UUID id, String key, String def)
			throws MException;

	SopObjectParameter getParameter(String type, UUID id, String key)
			throws MException;

	SopObjectParameter getParameter(Class<?> type, UUID id, String key)
			throws MException;

	void deleteParameters(Class<?> type, UUID id) throws MException;

	List<SopObjectParameter> getParameters(Class<?> type, String key, String value) throws MException;

	<T> LinkedList<T> collectResults(AQuery<T> asc, int page) throws MException;

	SopObjectParameter getRecursiveParameter(DbMetadata obj, String key) throws MException;

	List<SopActionTask> getActionTaskPage(String queue, int size);
	
	boolean canRead(DbMetadata obj) throws MException;
	boolean canUpdate(DbMetadata obj) throws MException;
	boolean canDelete(DbMetadata obj) throws MException;
//	boolean canCreate(Object parent, String newType) throws MException;
//	boolean canCreate(Object parent, Class<?> newType) throws MException;
	boolean canCreate(DbMetadata obj) throws MException;

	<T extends DbMetadata> T getObject(Class<T> type, UUID id) throws MException;
	<T extends DbMetadata> T getObject(String type, UUID id) throws MException;
	<T extends DbMetadata> T getObject(String type, String id) throws MException;
	
	Set<Entry<String, DbSchemaService>> getController();

	void onDelete(Persistable object);

	void collectRefereces(Persistable object, ReferenceCollector collector);

}
