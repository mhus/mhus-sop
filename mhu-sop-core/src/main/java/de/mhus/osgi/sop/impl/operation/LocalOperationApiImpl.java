package de.mhus.osgi.sop.impl.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VectorMap;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationException;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.operation.OperationsProvider;

@Component(immediate=true,provide=OperationsProvider.class,properties="provider=local")
public class LocalOperationApiImpl extends MLog implements OperationsProvider {

	static final String PROVIDER_NAME = "local";

	private BundleContext context;
	private ServiceTracker<Operation,Operation> nodeTracker;
	private HashMap<String, LocalOperationDescriptor> register = new HashMap<>();
	public static LocalOperationApiImpl instance;

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, Operation.class, new MyServiceTrackerCustomizer() );
		nodeTracker.open();
		instance = this;
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance  = null;
		context = null;
	}

	private class MyServiceTrackerCustomizer implements ServiceTrackerCustomizer<Operation,Operation> {

		@Override
		public Operation addingService(
				ServiceReference<Operation> reference) {

			Operation service = context.getService(reference);
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("register",desc);
					synchronized (register) {
						LocalOperationDescriptor descriptor = createDescriptor(reference, service);
						if (register.put(desc.getPath() + ":" + desc.getVersionString(), descriptor ) != null)
							log().w("Operation already defined",desc.getPath());
					}
				} else {
					log().i("no description found, not registered",reference.getProperty("objectClass"));
				}
			}
			return service;
		}

		private LocalOperationDescriptor createDescriptor(ServiceReference<Operation> reference, Operation service) {
			LinkedList<String> tags = new LinkedList<>();
			Object tagsStr = reference.getProperty("tags");
			if (tagsStr instanceof String[]) {
				for (String item : (String[])tagsStr)
					tags.add(item);
			} else
			if (tagsStr instanceof String) {
				for (String item : ((String)tagsStr).split(","))
					tags.add(item);
			}
			service.getDescription().getForm();
			OperationDescription desc = service.getDescription();
			
			return new LocalOperationDescriptor(OperationAddress.create(PROVIDER_NAME,desc), desc,tags, service);
		}

		@Override
		public void modifiedService(
				ServiceReference<Operation> reference,
				Operation service) {

			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("modified",desc);
					synchronized (register) {
						LocalOperationDescriptor descriptor = createDescriptor(reference, service);
						register.put(desc.getPath() + ":" + desc.getVersionString(), descriptor);
					}
				}
			}
			
		}

		@Override
		public void removedService(
				ServiceReference<Operation> reference,
				Operation service) {
			
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("unregister",desc);
					synchronized (register) {
						register.remove(desc.getPath() + ":" + desc.getVersionString());
					}
				}
			}			
		}
		
	}

	@Override
	public void collectOperations(List<OperationDescriptor> list, String filter, VersionRange version, Collection<String> providedTags) {
		synchronized (register) {
			for (OperationDescriptor desc : register.values()) {
				if (OperationUtil.matches(desc, filter, version, providedTags))
					list.add(desc);
			}
		}
	}

	@Override
	public OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags, IProperties properties, String ... executeOptions)
			throws NotFoundException {
		OperationDescriptor d = null;
		synchronized (register) {
			for (OperationDescriptor desc : register.values()) {
				if (OperationUtil.matches(desc, filter, version, providedTags)) {
						d = desc;
						break;
				}
			}
		}
		if (d == null) throw new NotFoundException("operation not found",filter,version,providedTags);
		return doExecute(d, properties);
	}

	@Override
	public OperationResult doExecute(OperationDescriptor desc, IProperties properties, String ... executeOptions) throws NotFoundException {
		Operation operation = null;
		if (desc instanceof LocalOperationDescriptor) {
			operation = ((LocalOperationDescriptor)desc).operation;
		}
		if (operation == null) {
			if (!PROVIDER_NAME.equals(desc.getProvider()))
				throw new NotFoundException("description is from another provider",desc);
			synchronized (register) {
				LocalOperationDescriptor local = register.get(desc.getPath() + ":" + desc.getVersionString());
				if (local != null)
					operation = local.operation;
			}
		}
		if (operation == null)
			throw new NotFoundException("operation not found", desc);
		
		DefaultTaskContext taskContext = new DefaultTaskContext(getClass());
		taskContext.setParameters(properties);
		try {
			return operation.doExecute(taskContext);
		} catch (OperationException e) {
			log().w(desc,properties,e);
			return new NotSuccessful(operation,e.getMessage(), e.getCaption(), e.getReturnCode());
		} catch (Exception e) {
			log().w(desc,properties,e);
			return new NotSuccessful(operation,e.toString(), OperationResult.INTERNAL_ERROR);
		}
	}

	private class LocalOperationDescriptor extends OperationDescriptor {

		private Operation operation;

		public LocalOperationDescriptor(OperationAddress address, OperationDescription description,
				Collection<String> tags, Operation operation) {
			super(address, description, tags);
			this.operation = operation;
		}
		
	}
	
}
