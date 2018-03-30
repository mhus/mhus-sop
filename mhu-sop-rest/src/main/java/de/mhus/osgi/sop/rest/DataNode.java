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
package de.mhus.osgi.sop.rest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.node.ObjectNode;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.data.SopDataController;
import de.mhus.osgi.sop.api.model.SopData;
import de.mhus.osgi.sop.api.model.SopFoundation;
import de.mhus.osgi.sop.api.rest.AbstractObjectListNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestException;
import de.mhus.osgi.sop.api.rest.RestNodeService;
import de.mhus.osgi.sop.api.rest.RestResult;

@Component(immediate=true,provide=RestNodeService.class)
public class DataNode extends AbstractObjectListNode<SopData>{

	Log log = Log.getLog(DataNode.class);
	
	@Override
	public String[] getParentNodeIds() {
		return new String[] {"foundation"};
	}

	@Override
	public String getNodeId() {
		return "data";
	}

	@Override
	protected List<SopData> getObjectList(CallContext callContext) throws MException {
		SopApi api = MApi.lookup(SopApi.class);
		
		SopFoundation orga = getObjectFromContext(callContext, SopFoundation.class);
		Boolean archived = false;
		Date due = null;
		String type = null;
		String search = callContext.getParameter(Node.SEARCH);
		{
			String v = callContext.getParameter("_archived");
			if (v != null) archived = MCast.toboolean(v, false);
			if (archived == true) archived = null;
		}
		{
			String v = callContext.getParameter("_due");
			if (v != null) due = MCast.toDate(v, null);
		}
		{
			type = callContext.getParameter("_type");
		}
		{
			if (search == null) search = "";
			for (String name : callContext.getParameterNames()) {
				if (name.startsWith("__") || !name.startsWith("_")) {
					if (!MString.isEmptyTrim(search)) search=search+",";
					search = search + (name.startsWith("__") ? name.substring(1) : name) + ":" + callContext.getParameter(name);
				}
			}
		}
		if (type == null && !AaaUtil.isCurrentAdmin()) {
			throw new RestException(OperationResult.USAGE, "no type specified");
		}
		List<SopData> ret = api.getSopData(orga.getId(), type, search, true, archived, due);
		
		if (type != null) {
			SopDataController control = api.getDataSyncControllerForType(type);
			if (control == null)
				throw new RestException(OperationResult.NOT_SUPPORTED, "Unknown type " + type );
			control.syncListBeforeLoad(orga, type, search, archived, due, ret);
		}
		
		
		
		// add needed object if not already in list ....
		String selectedId = callContext.getParameter("_selected");
		if (selectedId != null && MValidator.isUUID(selectedId)) {
			UUID id = UUID.fromString(selectedId);
			boolean found = false;
			for (SopData item : ret)
				if (item.getId().equals(id)) {
					found = true;
					break;
				}
			if (!found) {
				SopData obj = api.getSopData(orga.getId(), selectedId, true);
				ret.add(0, obj);
			}
		}
		return ret;
	}

	@Override
	public Class<SopData> getManagedClass() {
		return SopData.class;
	}

	@Override
	protected SopData getObjectForId(CallContext callContext, String id) throws Exception {
		SopApi api = MApi.lookup(SopApi.class);
		
		SopFoundation found = getObjectFromContext(callContext, SopFoundation.class);
		SopData data = null;
		String type = callContext.getParameter("_type");
		if (type == null) {
			int pos = id.indexOf(':');
			if (pos > 1) {
				type = id.substring(0, pos);
				id = id.substring(pos+1);
			}
		}
		if (type != null)
			data = api.getSopDataByForeignId(found.getId(), type, id);
		else
			data = api.getSopData(found.getId(), id, true);
		
		if (data != null && !data.isIsPublic()) return null;
		return data;
	}

	@Override
	protected void doPrepareForOutput(SopData obj, CallContext context, boolean listMode) throws MException {
		super.doPrepareForOutput(obj, context, listMode);
		String type = obj.getType();
		SopDataController control = MApi.lookup(SopApi.class).getDataSyncControllerForType(type);
		if (control != null) {
			control.doPrepareForOutput(obj, context, listMode);
		}
	}

	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		SopData data = getObjectFromContext(callContext, SopData.class);

		SopDataController handler = MApi.lookup(SopApi.class).getDataSyncControllerForType(data.getType());
		MProperties p = new MProperties();
		for (String name : callContext.getParameterNames())
			if (!name.startsWith("_") || name.startsWith("__"))
				p.put(name, callContext.getParameter(name));
				
		OperationResult res = handler.actionSopData(data,callContext.getAction(),p);
		
		JsonResult result = new JsonResult();
		ObjectNode jRoot = result.createObjectNode();
		if (res == null) return result;
		
		try {
			MPojo.pojoToJson(res, jRoot);
		} catch (IOException e) {
			log.e(e);
		}

		return result;
	}

}
