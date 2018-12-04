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

import java.util.List;
import java.util.Set;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.io.http.MHttp;

public class CallContext {

	public static final String ACTION_PARAMETER = "_action";

	private HttpRequest req;
	private MHttp.METHOD method;
	private IProperties context;
	
	public CallContext(HttpRequest req,
			MHttp.METHOD method, IProperties context) {
		this.req = req;
		this.method = method;
		this.context = context;
	}

	public boolean hasAction() {
		return req.getParameter(ACTION_PARAMETER) != null;
	}
	
	public String getAction() {
		return getParameter(ACTION_PARAMETER);
	}
	
	public String getParameter(String key) {
		String val = req.getParameter(key);
		return val;
	}

	public IProperties getParameters() {
		MProperties out = new MProperties();
		for (String n : getParameterNames())
			out.put(n, getParameter(n));
		return out;
	}
	
	public Object get(String key) {
		return context.get(key);
	}

	public HttpRequest getRequest() {
		return req;
	}

	public void put(String key, Object value) {
		context.put(key, value);
	}
	
	public String[] getNames() {
		return context.keySet().toArray(new String[0]);
	}

	public Set<String> getParameterNames() {
		return req.getParameterNames();
	}

	public MHttp.METHOD getMethod() {
		return method;
	}

	public Node lookup(List<String> parts, String lastNodeId)
			throws Exception {
        RestApi restService = MApi.lookup(RestApi.class);
        return restService.lookup(parts, lastNodeId, this);
	}

}
