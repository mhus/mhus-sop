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
package de.mhus.osgi.sop.api.action;

import java.util.List;
import java.util.Map;

import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.action.BpmCase.STATUS;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.RestResult;

public interface ActionApi extends SApi {

	BpmCase getCase(String id) throws MException;
	
	BpmDefinition getDefinition(String id) throws MException;
	
	List<BpmCase> getCases(STATUS status, int page) throws MException;
	
	List<BpmCase> getCases(String search) throws MException;
	
	BpmCase createCase(String customId, String process, Map<String, Object> parameters, boolean secure, long timeout) throws MException;
	
	BpmCase createCase(String customId, String process, Map<String, Object> parameters, String user, String pass, boolean secure, long timeout) throws MException;

	void delete(BpmCase item) throws MException;
	
	boolean syncBpm(BpmCase item, boolean forced) throws MException;

	long getUpdateInterval();

	void setUpdateInterval(long updateInterval);

	void prepareSecure(BpmCase bpm, boolean created) throws MException;
	
	List<BpmDefinition> getDefinitions(boolean dynamic) throws MException;
	
	/**
	 * Executes/Create a BPM Case. The Method is specially for REST execution.
	 * 
	 * @param callContext The call context of the request
	 * @param action A special action or null if the default call action should be used
	 * @param source The source on which node the is executed
	 * @return The result for the REST handling
	 * @throws MException
	 */
	RestResult doExecuteRestAction(CallContext callContext, String action, String source) throws MException;

}
