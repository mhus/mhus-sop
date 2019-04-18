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
package de.mhus.osgi.sop.foundation.rest;

import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.foundation.model.SopJournal;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonListNode;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true,service=RestNodeService.class)
public class JournalRestNode extends JsonListNode<JournalQueue>{

	@Override
	public String[] getParentNodeCanonicalClassNames() {
		return new String[] {ROOT_PARENT, FOUNDATION_PARENT};
	}

	@Override
	public String getNodeId() {
		return "journal";
	}

	@Override
	public void doRead(JsonResult result, CallContext callContext)
			throws Exception {

		
		JournalQueue queue = getObjectFromContext(callContext);
		if (queue == null) return;
		
		long since = MCast.tolong( callContext.getParameter("since"), -1);
		int max = MCast.toint( callContext.getParameter("size"), 0);
		if (max < 1 || max > 1000) max = 1000;
		int page = callContext.getParameter("page", 0);
		String search = callContext.getParameter("search");
		
		UUID foundationId = getObjectFromContext(callContext, SopFoundation.class).getId();
		
		FoundationApi api = M.l(FoundationApi.class);
		SopApi sop = M.l(SopApi.class);
		PojoModelFactory factory = sop.getDataPojoModelFactory();
		ArrayNode list = result.createArrayNode();
		List<SopJournal> res = api.getJournalEntries(foundationId, queue.getName(), since, max, page, search);
		for (SopJournal j : res) {
			ObjectNode obj = list.addObject();
			MPojo.pojoToJson(j, obj, factory, true);
		}
		
	}

//	@Override
//	public Class<JournalQueue> getManagedClass() {
//		return JournalQueue.class;
//	}

	@Override
	protected JournalQueue getObjectForId(CallContext context, String id) throws Exception {
		return new JournalQueue(id);
	}

}
