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

import java.lang.reflect.Method;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.pojo.MPojo;

public abstract class AbstractNode<T> extends MLog implements RestNodeService {

	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
		try {
			JsonResult result = new JsonResult();
			Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
			method.invoke(this, result, callContext);
			return result;
		} catch (Throwable t) {
			log().d(methodName,callContext,t);
			return null;
		}
	}

	@Override
	public String getDefaultAcl() {
		return null;
	}

}
