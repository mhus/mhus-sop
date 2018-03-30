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

import java.util.HashSet;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.errors.MException;

public class BpmDefinition extends DbMetadata {

	@DbPersistent
	@DbIndex("u1")
	private String process;
	@DbPersistent
	@DbIndex("u1")
	private String processor;
	@DbPersistent
	private HashSet<String> parameters = new HashSet<>();
	@DbPersistent
	private boolean enabled = true;
	@DbPersistent
	private MProperties options = new MProperties();
	
	public BpmDefinition() {
	}
	
	public BpmDefinition(String processor) {
		this.processor = processor;
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public HashSet<String> getParameters() {
		return parameters;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public MProperties getOptions() {
		return options;
	}

	public String getPublicFilter(boolean created) {
		return created ? options.getString("filter.created", null) : options.getString("filter.public", null);
	}

	public String getProcessor() {
		return processor;
	}
	
}
