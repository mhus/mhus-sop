package de.mhus.osgi.sop.rest;

import java.util.List;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopFoundation;
import de.mhus.osgi.sop.api.rest.AbstractObjectListNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true,provide=RestNodeService.class)
public class FoundationNode extends AbstractObjectListNode<SopFoundation> {

	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID};
	}

	@Override
	public String getNodeId() {
		return FOUNDATION_ID;
	}

	@Override
	protected List<SopFoundation> getObjectList(CallContext callContext) throws MException {
		return MApi.lookup(SopApi.class).searchFoundations(callContext.getParameter(Node.SEARCH));
	}

	@Override
	public Class<SopFoundation> getManagedClass() {
		return SopFoundation.class;
	}

	@Override
	protected SopFoundation getObjectForId(CallContext context, String id) throws Exception {
		return MApi.lookup(SopApi.class).getFoundation(id);
	}

}
