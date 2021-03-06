/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.foundation.rest;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.M;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.ObjectListNode;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate = true, service = RestNodeService.class)
public class FoundationNode extends ObjectListNode<SopFoundation, SopFoundation> {

    @Override
    public String[] getParentNodeCanonicalClassNames() {
        return new String[] {ROOT_PARENT};
    }

    @Override
    public String getNodeId() {
        return FOUNDATION_NODE_NAME;
    }

    @Override
    protected List<SopFoundation> getObjectList(CallContext callContext) throws MException {
        FoundationApi api = M.l(FoundationApi.class);
        int page = callContext.getParameter("page", 0);
        return api.searchFoundations(callContext.getParameter("search"), page);
    }

    //	@Override
    //	public Class<SopFoundation> getManagedClass() {
    //		return SopFoundation.class;
    //	}

    @Override
    protected SopFoundation getObjectForId(CallContext context, String id) throws Exception {
        FoundationApi api = M.l(FoundationApi.class);
        return api.getFoundation(id);
    }
}
