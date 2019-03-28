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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true)
public class RestApiImpl extends MLog implements RestApi {

	private BundleContext context;
	private ServiceTracker<RestNodeService,RestNodeService> nodeTracker;
	private HashMap<String, RestNodeService> register = new HashMap<>();

	public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, RestNodeService.class, new RestNodeServiceTrackerCustomizer() );
		nodeTracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		nodeTracker.close();
		context = null;
		nodeTracker = null;
		register.clear();
	}

	private class RestNodeServiceTrackerCustomizer implements ServiceTrackerCustomizer<RestNodeService,RestNodeService> {

		@Override
		public RestNodeService addingService(
				ServiceReference<RestNodeService> reference) {

			RestNodeService service = context.getService(reference);
			if (service != null) {
				for (String x : service.getParentNodeCanonicalClassNames()) {
					if (x != null) {
					    if (!x.contains(".")) // print a warning - class name without dot should be a mistake
					        log().w("Register RestNode with malformed parent name - should be a class",service.getClass(),service.getNodeId(),x);
						String key = x + "-" + service.getNodeId();
						log().i("register",key,service.getClass().getCanonicalName());
						register.put(key,service);
					}
				}
			}
			
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<RestNodeService> reference,
				RestNodeService service) {
			
		}

		@Override
		public void removedService(ServiceReference<RestNodeService> reference,
				RestNodeService service) {
			
			if (service != null) {
				for (String x : service.getParentNodeCanonicalClassNames()) {
					if (x != null) {
						String key = x + "-" + service.getNodeId();
						log().i("unregister",key,service.getClass().getCanonicalName());
						register.remove(key);
					}
				}
			}

		}
		
	}
	
	@Override
	public Map<String, RestNodeService> getRestNodeRegistry() {
		return register;
	}

	@Override
	public Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context) throws Exception {
		if (parts.size() < 1) return null;
		String name = parts.get(0);
		parts.remove(0);
		String lastNodeId = lastNode == null ? RestNodeService.ROOT_PARENT : lastNode.getCanonicalName();
		RestNodeService next = register.get(lastNodeId + "-" + name); 
		if (next == null) return null;
		
		AccessApi aaa = MApi.lookup(AccessApi.class);
		if (aaa != null) {
			try {
				String def = next.getDefaultAcl();
				if (!aaa.hasResourceAccess(aaa.getCurrentAccount(), "rest.node", name, "execute", def))
					throw new AccessDeniedException("access denied");
			} catch (Throwable t) {
				throw new AccessDeniedException("internal error", t);
			}
		} else
		if (!RELAXED.value())
			throw new AccessDeniedException("Access api not found");
		
		return next.lookup(parts, context);
	}
	
}
