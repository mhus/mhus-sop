package de.mhus.osgi.sop.api.operation;

import java.util.HashSet;
import java.util.List;

public class SelectorRoundRobin implements Selector {
	
	private HashSet<String> done = new HashSet<>();
	
	@Override
	public void select(List<OperationDescriptor> list) {
		if (list.size() <= 1) return;
		int cnt = 0;
		while (true) {
			if (cnt >= list.size()) // reset already done list
				done.clear();
			OperationDescriptor item = list.get(cnt);
			String addr = item.getAddress().toString();
			if (!done.contains(addr)) {
				done.add(addr);
				list.clear();
				list.add(item);
				return;
			}
			cnt++;
		}
		
	}

}
