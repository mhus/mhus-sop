package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.SApi;

public interface OperationApi extends SApi {

	List<OperationAddress> getOperations(String filter, VersionRange version);
	
	Operation getOperation(OperationAddress address) throws NotFoundException;
	
}
