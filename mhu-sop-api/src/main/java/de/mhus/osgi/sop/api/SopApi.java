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
package de.mhus.osgi.sop.api;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.data.SopDataController;
import de.mhus.osgi.sop.api.model.SopAcl;
import de.mhus.osgi.sop.api.model.SopData;
import de.mhus.osgi.sop.api.model.SopFoundation;
import de.mhus.osgi.sop.api.model.SopFoundationGroup;
import de.mhus.osgi.sop.api.model.SopJournal;

public interface SopApi extends SApi {

	IProperties getMainConfiguration();

	SopJournal appendJournalEntry(UUID foundation, String queue, String event, String ... data) throws MException;

	SopJournal getJournalEntry(String id) throws MException;

	PojoModelFactory getDataPojoModelFactory();

	List<SopJournal> getJournalEntries(String queue, long since, int max) throws MException;

	SopFoundationGroup getFoundationGroup(String group) throws MException;

	DbMetadata getFoundation(UUID id) throws MException;

	UUID getDefaultFoundationId();

	SopAcl getAcl(String id) throws MException;
	
	XdbService getManager();

	SopDataController getDataSyncControllerForType(String type);

	List<SopData> getSopData(UUID orgaId, String type, String search, boolean publicAccess, Boolean archived, Date due) throws MException;

	SopData getSopData(UUID orgaId, String id, boolean sync) throws MException;

	SopData getSopData(UUID orgaId, UUID id, boolean sync) throws MException;

	SopData getSopDataByForeignId(UUID orgaId, String type, String id) throws MException;

	boolean syncSopData(SopData obj, boolean forced, boolean save);

	List<SopFoundation> searchFoundations(String search) throws MException;

	SopFoundation getFoundation(String id) throws MException;

}
