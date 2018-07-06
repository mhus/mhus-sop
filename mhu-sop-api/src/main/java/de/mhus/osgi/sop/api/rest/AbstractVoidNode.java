package de.mhus.osgi.sop.api.rest;

import java.util.List;

/**
 * Use this super class to implement a node without data. The node only implements
 * actions.
 * 
 * @author mikehummel
 *
 */
public abstract class AbstractVoidNode extends AbstractSingleObjectNode<Void>  {

	@Override
	public Node lookup(List<String> parts, CallContext callContext)
			throws Exception {
		
		if (parts.size() < 1) return this;
		return callContext.lookup(parts, getNodeId());
	}
	
	@Override
	public final Class<Void> getManagedClass() {
		return Void.class;
	}

	@Override
	protected final Void getObject(CallContext context) throws Exception {
		return null;
	}

}
