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

import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;

public abstract class SingleObjectNode<T> extends JsonSingleNode<T> {

	@Override
	public void doRead(JsonResult result, CallContext callContext)
			throws Exception {

		PojoModelFactory schema = getPojoModelFactory();
		
		T obj = getObjectFromContext(callContext, getManagedClass());
		if (obj == null)
			throw new NotFoundException();
		
		doPrepareForOutput(obj, callContext, false);
		ObjectNode jRoot = result.createObjectNode();
		MPojo.pojoToJson(obj, jRoot, schema, true);

		
	}

	protected PojoModelFactory getPojoModelFactory() {
		return RestUtil.getPojoModelFactory();
	}

	protected void doPrepareForOutput(T obj, CallContext context, boolean listMode) throws MException {
	}

	@Override
	protected void doUpdate(JsonResult result, CallContext callContext)
			throws Exception {
		T obj = getObjectFromContext(callContext);
		if (obj == null) throw new RestException(OperationResult.NOT_FOUND);
		
		RestUtil.updateObject(callContext, obj, true);
	}

}
