package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.crypt.MRandom;

public class SelectorRandom implements Selector {

	@Override
	public void select(List<OperationDescriptor> list) {
		if (list.size() <= 1) return;
		MRandom random = MApi.lookup(MRandom.class);
		int pos = random.getInt() % list.size();
		OperationDescriptor item = list.get(pos);
		list.clear();
		list.add(item);
	}

}
