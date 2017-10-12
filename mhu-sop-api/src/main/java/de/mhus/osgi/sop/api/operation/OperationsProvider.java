package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public interface OperationsProvider {

	void collectOperations(List<OperationAddress> list,String filter, VersionRange version);
	
	Operation getOperation(OperationAddress address) throws NotFoundException;

}
