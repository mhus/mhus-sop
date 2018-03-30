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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;

public class BpmCase extends DbMetadata implements FoundationRelated {

	public enum STATUS {NEW,PROGRESS,CLOSED,ERROR}
	
	@DbPersistent(ro=true)
	@DbIndex({"1"})
	private UUID foundation;
	@DbPersistent
	@DbIndex({"u2"})
	private String bpmId;
	@DbPersistent(ro=true)
	private String process;
	@DbPersistent(ro=true)
	@DbIndex({"u2"})
	private String processor;
	@DbPersistent(ro=true)
	private String mapped;
	@DbPersistent
	private STATUS status;
	@DbPersistent
	private String msg;
	@DbPersistent(ro=true)
	private String customId;
	@DbPersistent
	private Date lastSync;
	@DbPersistent
	private MProperties parameters;
	@DbPersistent
	private MProperties initial;
	@DbPersistent
	private long statusCode;
	@DbPersistent
	private LinkedList<String> comments;
	
	public BpmCase() {}

	
	public BpmCase(UUID foundation, String bpmId, String processor, String process, String mapped, String customId, Map<String, Object> parameters) {
		super();
		this.foundation = foundation;
		this.bpmId = bpmId;
		this.processor = processor;
		this.process = process;
		this.mapped = mapped;
		this.customId = customId;
		this.status = STATUS.NEW;
		this.initial = new MProperties(parameters);
	}


	public String getBpmId() {
		return bpmId;
	}

	public String getProcess() {
		return process;
	}

	public String getMappedProcess() {
		return mapped;
	}
	
	public STATUS getStatus() {
		return status;
	}

	public String getMsg() {
		return msg;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCustomId() {
		return customId;
	}

	public Date getLastSync() {
		return lastSync;
	}


	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}


	public MProperties getParameters() {
		return parameters;
	}


	public void setParameters(MProperties parameters) {
		this.parameters = parameters;
	}


	public IReadProperties getInitial() {
		return initial;
	}

	public String toString() {
		return MSystem.toString(this, getId(), process, bpmId );
	}


	public void setBpmId(String bpmId) {
		this.bpmId = bpmId;
	}

	public long getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(long statusCode) {
		this.statusCode = statusCode;
	}


	public List<String> getComments() {
		if (comments == null) comments = new LinkedList<String>();
		return Collections.unmodifiableList(comments);
	}	

	public void appendComment(Object source, Object ... msg) {
		String sourceName = null;
		if (source == null)
			sourceName = "?";
		else
		if (source instanceof Class<?>)
			sourceName = ((Class<?>)source).getCanonicalName();
		else
		if (source instanceof String)
			sourceName = (String)source;
		else
			sourceName = source.getClass().getCanonicalName();

		StringBuilder m = new StringBuilder().append(MDate.toIso8601(new Date())).append('|').append(sourceName);
		if (msg != null) {
			for (Object x : msg)
				if (x != null) m.append('|').append(MCast.toString(x));
		}
		if (comments == null) comments = new LinkedList<String>();
		comments.add(m.toString());
	}


	public void clearComments() {
		if (comments == null) return;
		comments.clear();
		setDbHandler(null);
	}

	public String getProcessor() {
		return processor;
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
