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

public abstract class AbstractSingleNode<T> implements RestNodeService {

	public static final String ID 		= "_id";
	public static final String OBJECT 	= "_obj";
	public static final String SOURCE 	= "source";
	public static final String INTERNAL_PREFIX = "_";

	@Override
	public Node lookup(List<String> parts, CallContext callContext)
			throws Exception {
		
		T obj = getObject(callContext);

		if (obj == null) return null;
		
		callContext.put(getManagedClass().getCanonicalName() + OBJECT, obj);

		if (parts.size() < 1) return this;

		return callContext.lookup(parts, getNodeId());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getObjectFromContext(CallContext callContext, Class<T> clazz) {
		return (T) callContext.get(clazz.getCanonicalName() + OBJECT);
	}

	@SuppressWarnings("unchecked")
	protected T getObjectFromContext(CallContext callContext) {
		return (T) callContext.get(getManagedClass().getCanonicalName() + OBJECT);
	}
 
	/**
	 * Return a the managed class as class
	 * @return x
	 */
	public abstract Class<T> getManagedClass();
	
	protected abstract T getObject(CallContext context) throws Exception;

	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		return RestUtil.doExecuteRestAction( callContext, null, getNodeId());
	}

}