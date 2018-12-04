package de.mhus.osgi.sop.api.rest;

import java.io.PrintWriter;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;

public class PojoResult implements RestResult {

	private String contentType;
	private Object obj;

	public PojoResult(Object obj, String contentType) {
		if (MString.isEmpty(contentType)) contentType = MHttp.CONTENT_TYPE_JSON;
		this.contentType = contentType;
		this.obj = obj;
	}

	@Override
	public void write(PrintWriter writer) throws Exception {
		if (obj == null) return;
		JsonResult json = new JsonResult();
		PojoModelFactory factory = MPojo.getDefaultModelFactory();
		if (obj.getClass().isArray()) {
			if (obj.getClass().getComponentType().isPrimitive()) {
				ArrayNode jArray = json.createArrayNode();
				for (Object aObj : (Object[])obj) {
					if (aObj == null) continue; // should not happen
					MPojo.addJsonValue(jArray, aObj, factory, true, false, 0);
				}
			} else {
				ArrayNode jArray = json.createArrayNode();
				for (Object aObj : (Object[])obj) {
					ObjectNode jObj = jArray.objectNode();
					if (aObj != null)
						MPojo.pojoToJson(aObj, jObj, factory);
					jArray.add(jObj);
				}
			}
		} else 
		if (obj.getClass().isPrimitive()) {
			ObjectNode jObj = json.createObjectNode();
			MPojo.setJsonValue(jObj, "value", obj, factory, true, false, 0);
		} else {
			ObjectNode jObj = json.createObjectNode();
			MPojo.pojoToJson(obj, jObj, factory);
		}
		json.write(writer);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

}
