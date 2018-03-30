/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
