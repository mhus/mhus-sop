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
package de.mhus.osgi.sop.api.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.model.SopData;
import de.mhus.osgi.sop.api.model.SopFoundation;
import de.mhus.osgi.sop.api.rest.CallContext;

public interface SopDataController {

	/**
	 * Called from REST if a SopData should be created. The object is fully filled
	 * the method has to call the 'save()' method.
	 * 
	 * @param data
	 * @throws Exception
	 */
	void createSopData(SopData data) throws Exception;

	/**
	 * Called from REST if a SopData should be updated. The object is fully filled/updated
	 * the method has to call the 'save()' method.
	 * 
	 * @param data
	 * @throws Exception
	 */
	void updateSopData(SopData data) throws Exception;
	
	/**
	 * Called by HfoApi to check if a sync is needed. The method will not be called fi the sync is forced.
	 * 
	 * @param obj
	 * @return
	 */
	boolean isNeedSync(SopData obj);

	/**
	 * Called by the REST before a object is transformed to JSON. Give the ability to remove
	 * confidential data. The object will not be saved after this call.
	 * 
	 * @param obj
	 * @param context
	 * @param listMode
	 * @throws MException
	 */
	void doPrepareForOutput(SopData obj, CallContext context, boolean listMode) throws MException;

	/**
	 * Called by the REST before a list of SopData is loaded. Needed to sync all objects or invalidate objects
	 * before loading the data.
	 * 
	 * @param orga
	 * @param type
	 * @param search
	 * @param archived
	 * @param due
	 * @param ret 
	 */
	void syncListBeforeLoad(SopFoundation found, String type, String search, Boolean archived, Date due, List<SopData> list);

	void deleteSopData(SopData data) throws Exception;

	/**
	 * Action from rest node routed to the controller.
	 * 
	 * @param data
	 * @param action
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	OperationResult actionSopData(SopData data, String action, IProperties parameters) throws Exception;

	/**
	 * Action from the the SopDataOperation routed to the controller.
	 * 
	 * @param data
	 * @param p
	 * @return
	 * @throws Exception 
	 */
	OperationResult actionSopDataOperation(SopData data, String action, IProperties p) throws Exception;

	void synchronizeSopData(SopData obj);
	
}
