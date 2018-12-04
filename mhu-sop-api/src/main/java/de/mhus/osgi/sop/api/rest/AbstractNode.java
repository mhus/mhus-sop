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
package de.mhus.osgi.sop.api.rest;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.errors.UsageException;
import de.mhus.osgi.sop.api.rest.annotation.RestAction;
import de.mhus.osgi.sop.api.rest.annotation.RestNode;

public abstract class AbstractNode extends MLog implements RestNodeService {

	private RestNode nodeDef;
	private HashMap<String, Method> actions = null;

	@Override
	public Node lookup(List<String> parts, CallContext callContext) throws Exception {
		return this;
	}


	@Override
	public RestResult doCreate(CallContext arg0) throws Exception {
		return null;
	}

	@Override
	public RestResult doDelete(CallContext arg0) throws Exception {
		return null;
	}
	
	@Override
	public RestResult doUpdate(CallContext call) throws Exception {
		return null;
	}

	public AbstractNode() {
		nodeDef = getClass().getAnnotation(RestNode.class);
		for (Method method : MSystem.getMethods(getClass())) {
			RestAction action = method.getAnnotation(RestAction.class);
			if (actions == null)
				actions = new HashMap<>();
			actions.put(action.name(), method);
		}
	}
	
	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		if (actions != null) {
			String actionName = callContext.getAction();
			try {
				Method action = actions.get(actionName);
				if (action != null) {
					Object res = action.invoke(this, callContext);
					if (res == null) 
						return null;
					if (res instanceof RestResult)
						return (RestResult)res;
					if (res instanceof InputStream)
						return new BinaryResult((InputStream)res, action.getAnnotation(RestAction.class).contentType());
					if (res instanceof Reader)
						return new BinaryResult((Reader)res, action.getAnnotation(RestAction.class).contentType());
					if (res instanceof String)
						return new PlainTextResult((String)res, action.getAnnotation(RestAction.class).contentType());
					return new PojoResult(res, action.getAnnotation(RestAction.class).contentType());
				} else {
					log().d("action unknown",actionName);
				}
			} catch (Throwable t) {
				log().d(actionName,callContext,t);
			}
		} else {
			String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
			try {
				JsonResult result = new JsonResult();
				Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
				method.invoke(this, result, callContext);
				return result;
			} catch (java.lang.NoSuchMethodException e) {
				log().d("action method not found",methodName);
			} catch (Throwable t) {
				log().d(methodName,callContext,t);
			}
		}
		return null;
	}

	// root by default
	@Override
	public String[] getParentNodeIds() {
		if (nodeDef == null)
			throw new UsageException("parent node not defined");
		if (nodeDef.parentNode().length != 0) {
			String[] out = new String[nodeDef.parentNode().length];
			for (int i = 0; i < out.length; i++) {
				RestNode parentNodeDef = nodeDef.parentNode()[i].getAnnotation(RestNode.class);
				if (parentNodeDef != null)
					out[i] = parentNodeDef.name();
			}
			return out;
		}
		return nodeDef.parent();
	}

	@Override
	public String getNodeId() {
		if (nodeDef == null)
			throw new UsageException("parent node not defined");
		return nodeDef.name();
	}

	@Override
	public String getDefaultAcl() {
		if (nodeDef == null)
			return null;
		return nodeDef.acl();
	}

}
