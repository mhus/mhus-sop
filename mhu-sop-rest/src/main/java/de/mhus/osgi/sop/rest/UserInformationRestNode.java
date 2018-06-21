package de.mhus.osgi.sop.rest;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.rest.AbstractSingleObjectNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true,provide=RestNodeService.class)
public class UserInformationRestNode extends AbstractSingleObjectNode<UserInformation>{

	@Override
	public String[] getParentNodeIds() {
		return new String[] {PUBLIC_ID};
	}

	@Override
	public String getNodeId() {
		return "uid";
	}

	@Override
	public Class<UserInformation> getManagedClass() {
		return UserInformation.class;
	}

	@Override
	protected UserInformation getObject(CallContext context) throws Exception {
		AccessApi aaa = MApi.lookup(AccessApi.class);
		if (aaa == null) throw new NotFoundException("AccessApi not configured");
		
		AaaContext acc = aaa.getCurrentOrGuest();
		
		return new UserInformation(acc);
	}
	
}
