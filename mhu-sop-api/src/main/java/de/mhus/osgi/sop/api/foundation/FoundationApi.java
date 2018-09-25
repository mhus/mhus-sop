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
package de.mhus.osgi.sop.api.foundation;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.data.SopDataController;
import de.mhus.osgi.sop.api.foundation.model.SopData;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.foundation.model.SopFoundationGroup;
import de.mhus.osgi.sop.api.foundation.model.SopJournal;

public interface FoundationApi {

	public static final String DEFAULT_GROUP = "";
	
	SopJournal appendJournalEntry(UUID foundation, String queue, String event, String ... data) throws MException;

	List<SopJournal> getJournalEntries(UUID foundation, String queue, long since, int max) throws MException;

	SopJournal getJournalEntry(String id) throws MException;

	SopFoundationGroup getFoundationGroup(String group) throws MException;

	SopFoundation getFoundation(UUID id) throws MException;

	UUID getDefaultFoundationId();

	SopDataController getDataSyncControllerForType(String type);

	List<SopData> getSopData(UUID orgaId, String type, String search, boolean publicAccess, Boolean archived, Date due, String order, int size) throws MException;

	SopData getSopData(UUID orgaId, String id, boolean sync) throws MException;

	SopData getSopData(UUID orgaId, UUID id, boolean sync) throws MException;

	SopData getSopDataByForeignId(UUID orgaId, String type, String id) throws MException;

	boolean syncSopData(SopData obj, boolean forced, boolean save);

	List<SopFoundation> searchFoundations(String search) throws MException;

	SopFoundation getFoundation(String id) throws MException;

}
