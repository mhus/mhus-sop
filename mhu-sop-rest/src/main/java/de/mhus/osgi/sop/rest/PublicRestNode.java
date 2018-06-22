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

import java.lang.reflect.Method;

import org.codehaus.jackson.node.ObjectNode;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.osgi.sop.api.rest.AbstractSingleObjectNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestNodeService;
import de.mhus.osgi.sop.api.rest.RestResult;

@Component(immediate=true,provide=RestNodeService.class)
public class PublicRestNode extends AbstractSingleObjectNode<Object> {
	
	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID};
	}

	@Override
	public String getNodeId() {
		return PUBLIC_ID;
	}

	@Override
	public Class<Object> getManagedClass() {
		return Object.class;
	}

	@Override
	protected Object getObject(CallContext callContext) throws Exception {
		return new Object();
	}


	public void onPing(JsonResult result, CallContext callContext) throws Exception {
		ObjectNode o = result.createObjectNode();
		o.put("msg", "pong");
	}

	public void onSleep(JsonResult result, CallContext callContext) throws Exception {
		MThread.sleep( MCast.tolong(callContext.getParameter("sleep"), 0 ));
		ObjectNode o = result.createObjectNode();
		o.put("msg", "pong");
	}
	
	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
		JsonResult result = new JsonResult();
		Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
		method.invoke(this, result, callContext);
		return result;
	}

}
