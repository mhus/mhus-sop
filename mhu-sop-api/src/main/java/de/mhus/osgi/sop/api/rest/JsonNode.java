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

import de.mhus.lib.errors.NotSupportedException;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;

public abstract class JsonNode<T> extends AbstractNode<T>{

	@Override
	public RestResult doRead(CallContext callContext) throws Exception {
		JsonResult result = new JsonResult();
		doRead(result, callContext);
		return result;
	}

	public abstract void doRead(JsonResult result, CallContext callContext) throws Exception;

	@Override
	public RestResult doCreate(CallContext callContext) throws Exception {
		OperationDescriptor oper = getCreateAction();
		if (oper != null)
			return super.doCreate(callContext);
		JsonResult result = new JsonResult();
		doCreate(result, callContext);
		return result;
	}

	@Override
	public RestResult doUpdate(CallContext callContext) throws Exception {
		OperationDescriptor oper = getUpdateAction();
		if (oper != null)
			return super.doUpdate(callContext);
		JsonResult result = new JsonResult();
		doUpdate(result, callContext);
		return result;
	}

	@Override
	public RestResult doDelete(CallContext callContext) throws Exception {
		OperationDescriptor oper = getDeleteAction();
		if (oper != null)
			return super.doDelete(callContext);
		JsonResult result = new JsonResult();
		doDelete(result, callContext);
		return result;
	}
	
	protected void doUpdate(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	protected void doCreate(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	protected void doDelete(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	
	/*		
	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
		JsonResult result = new JsonResult();
		Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
		method.invoke(this, result, callContext);
		return result;
		
	}
 */		

	/**
	 * Overwrite the method if you need the feature. Instead use doCreate
	 */
	@Override
	protected OperationDescriptor getCreateAction() {
		return null;
	}

	/**
	 * Overwrite the method if you need the feature. Instead use doUpdate
	 */
	@Override
	protected OperationDescriptor getUpdateAction() {
		return null;
	}
	
	/**
	 * Overwrite the method if you need the feature. Instead use doDelete
	 */
	@Override
	protected OperationDescriptor getDeleteAction() {
		return null;
	}

}
