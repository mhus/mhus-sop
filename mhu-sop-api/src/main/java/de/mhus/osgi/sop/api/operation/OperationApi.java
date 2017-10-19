package de.mhus.osgi.sop.api.operation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.SApi;

public interface OperationApi extends SApi {

	static final String DEFAULT_PROVIDER_NAME = "local";

	List<OperationDescriptor> findOperations(String filter, VersionRange version, Collection<String> providedTags);
	OperationDescriptor findOperation(String filter, VersionRange version, Collection<String> providedTags) throws NotFoundException;
	List<OperationDescriptor> findOperations(OperationAddress addr, Collection<String> providedTags);
	OperationDescriptor findOperation(OperationAddress addr, Collection<String> providedTags) throws NotFoundException;
	
	OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags, IProperties properties, String ... executeOptions) throws NotFoundException;
	OperationResult doExecute(OperationDescriptor desc, IProperties properties, String ... executeOptions) throws NotFoundException;
	
}
