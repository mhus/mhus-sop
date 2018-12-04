package de.mhus.osgi.sop.api.rest;

import java.lang.reflect.Method;
import java.util.List;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.rest.anno.RestGet;
import de.mhus.osgi.sop.api.rest.anno.RestNode;

public class RoutingNode extends MLog implements RestNodeService {

	private RestNode nodeDef;

	public RoutingNode() throws MException {
		// parse
		nodeDef = getClass().getAnnotation(RestNode.class);
		if (nodeDef == null)
			throw new MException("Not a RestNode",getClass());
		for (Method method : MSystem.getMethods(getClass())) {
			RestGet getNode = method.getAnnotation(RestGet.class);
			if (getNode != null) {
				String[] path = getNode.path().split("/");
				
			}
		}
	}
	
	@Override
	public Node lookup(List<String> parts, CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestResult doRead(CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestResult doCreate(CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestResult doUpdate(CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestResult doDelete(CallContext callContext) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	// root by default
	@Override
	public String[] getParentNodeIds() {
		return new String[] {nodeDef.parent()};
	}

	@Override
	public String getNodeId() {
		return nodeDef.name();
	}

	@Override
	public String getDefaultAcl() {
		return nodeDef.acl();
	}

}
