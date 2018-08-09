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
