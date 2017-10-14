package de.mhus.osgi.sop.api.operation;

import java.util.Collection;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public interface OperationsProvider {

	void collectOperations(List<OperationDescriptor> list, String filter, VersionRange version, Collection<String> providedTags);
	
	OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags, IProperties properties, String ... executeOptions) throws NotFoundException;
	OperationResult doExecute(OperationDescriptor desc, IProperties properties, String ... executeOptions) throws NotFoundException;

}
