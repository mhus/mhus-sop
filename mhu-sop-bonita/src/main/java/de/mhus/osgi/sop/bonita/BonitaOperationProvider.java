package de.mhus.osgi.sop.bonita;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationException;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.operation.OperationsProvider;

@Component
public class BonitaOperationProvider extends MLog implements OperationsProvider {

	private static final String PROVIDER_NAME = "bonita";
	private HashMap<String, BonitaOperationDescriptor> register = new HashMap<>();

	@Activate
	public void doActivate(ComponentContext ctx) {
		
	}

	@Override
	public void collectOperations(List<OperationDescriptor> list, String filter, VersionRange version, Collection<String> providedTags) {
		update();
		synchronized (register) {
			for (OperationDescriptor desc : register.values()) {
				if (OperationUtil.matches(desc, filter, version, providedTags))
					list.add(desc);
			}
		}
	}

	@Override
	public OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags,
			IProperties properties, String... executeOptions) throws NotFoundException {
		OperationDescriptor d = null;
		update();
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

	private void update() {
		
	}

	@Override
	public OperationResult doExecute(OperationDescriptor desc, IProperties properties, String... executeOptions)
			throws NotFoundException {
		Operation operation = null;
		if (desc instanceof BonitaOperationDescriptor) {
			operation = ((BonitaOperationDescriptor)desc).operation;
		}
		if (operation == null) {
			if (!PROVIDER_NAME.equals(desc.getProvider()))
				throw new NotFoundException("description is from another provider",desc);
			update();
			synchronized (register) {
				BonitaOperationDescriptor local = register.get(desc.getPath() + ":" + desc.getVersionString());
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
	
	private class BonitaOperationDescriptor extends OperationDescriptor {

		private Operation operation;

		public BonitaOperationDescriptor(OperationAddress address, OperationDescription description,
				Collection<String> tags, Operation operation) {
			super(address, description, tags);
			this.operation = operation;
		}
		
	}

	@Override
	public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void synchronize() {
		// TODO Auto-generated method stub
		
	}


}
