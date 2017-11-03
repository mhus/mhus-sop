package de.mhus.osgi.sop.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
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
				for (String x : service.getParentNodeIds()) {
					String key = x + "-" + service.getNodeId();
					log().i("register",key,service.getClass().getCanonicalName());
					register.put(key,service);
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
				for (String x : service.getParentNodeIds()) {
					String key = x + "-" + service.getNodeId();
					log().i("unregister",key,service.getClass().getCanonicalName());
					register.remove(key);
				}
			}

		}
		
	}
	
	@Override
	public Map<String, RestNodeService> getRestNodeRegistry() {
		return register;
	}

	@Override
	public Node lookup(List<String> parts, String lastNodeId, CallContext context) throws Exception {
		if (parts.size() < 1) return null;
		String name = parts.get(0);
		parts.remove(0);
		if (lastNodeId == null) lastNodeId = RestNodeService.ROOT_ID;
		RestNodeService next = register.get(lastNodeId + "-" + name); 
		if (next == null) return null;
		
		AccessApi aaa = MApi.lookup(AccessApi.class);
		if (aaa != null) {
			try {
				if (!aaa.hasResourceAccess(aaa.getCurrenAccount(), "rest.node", name, "execute"))
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
