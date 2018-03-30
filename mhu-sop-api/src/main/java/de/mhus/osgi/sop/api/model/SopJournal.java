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
package de.mhus.osgi.sop.api.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.model.AttributeFeatureCut;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;

public class SopJournal extends DbMetadata implements FoundationRelated {

	public static final String QUEUE_BPM = "bpm";
	
	@DbPersistent(ro=true)
	@DbIndex({"u1","3"})
	private UUID foundation;
	@DbPersistent(ro=true)
	@DbIndex({"u1","2"})
	private long order;
	@DbPersistent(size=10,ro=true)
	@DbIndex({"u1"})
	private String queue;
	@DbPersistent(ro=true,features=AttributeFeatureCut.NAME)
	private String event;
	@DbPersistent(ro=true,features=AttributeFeatureCut.NAME)
	private HashMap<String,String> data;

	public SopJournal() {}
	
	public SopJournal(UUID foundation, String queue, String event, long order, String ... data) {
		this.foundation = foundation;
		this.queue = queue;
		this.event = event;
		this.order = order;
		this.data = new HashMap<>();
		if (data != null)
			for (int i = 0; i < data.length-1; i+=2)
				this.data.put(data[i], data[i+1]);
	}
	
	public String getQueue() {
		return queue;
	}

	public String getEvent() {
		return event;
	}

	public Map<String,String> getData() {
		return data;
	}

	public long getOrder() {
		return order;
	}

	@Override
	public UUID getFoundation() {
		return foundation;
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		return MApi.lookup(SopApi.class).getFoundation(getFoundation());
	}

}
