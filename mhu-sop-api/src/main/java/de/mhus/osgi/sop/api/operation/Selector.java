package de.mhus.osgi.sop.api.operation;

import java.util.List;

public interface Selector {

	void select(List<OperationDescriptor> list);

}
