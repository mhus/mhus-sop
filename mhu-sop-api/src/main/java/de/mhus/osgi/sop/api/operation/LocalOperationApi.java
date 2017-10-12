package de.mhus.osgi.sop.api.operation;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.SApi;

public interface LocalOperationApi extends SApi {

	OperationDescriptor getOperation(String path, VersionRange version) throws NotFoundException;
	
	OperationResult doExecute(String path, VersionRange version, IProperties properties) throws NotFoundException;

	OperationDescriptor[] getLocalOperations();
	
}
