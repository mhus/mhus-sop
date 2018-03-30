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
