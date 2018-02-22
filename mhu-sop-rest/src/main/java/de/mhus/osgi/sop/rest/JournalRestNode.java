package de.mhus.osgi.sop.rest;

import java.util.List;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.Journal;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonNode;
import de.mhus.osgi.sop.api.rest.JsonResult;

public class JournalRestNode extends JsonNode<JournalQueue>{

	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID};
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
		
		long since = MCast.tolong( callContext.getParameter("_since"), 0);
		
		SopApi api = MApi.lookup(SopApi.class);
		DbSchema schema = api.getDataSchema();
		ArrayNode list = result.createArrayNode();
		List<Journal> res = api.getJournalEntries(queue.getName(), since, 100);
		for (Journal j : res) {
			ObjectNode obj = list.addObject();
			MPojo.pojoToJson(j, obj, schema);
		}
		
	}

	@Override
	public Class<JournalQueue> getManagedClass() {
		return JournalQueue.class;
	}

	@Override
	protected JournalQueue getObjectForId(CallContext context, String id) throws Exception {
		return new JournalQueue(id);
	}

}
