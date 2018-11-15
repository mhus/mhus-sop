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
package de.mhus.osgi.sop.api.foundation.model;

import java.util.Date;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.model.AttributeFeatureCut;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.FoundationRelated;

@GenerateConst
public class SopData extends DbMetadata implements FoundationRelated {
	
	@DbPersistent(ro=true)
	@DbIndex({"parent","t1","t2"})
	private UUID foundation;
	
	// REST access restriction only !!!
	@DbPersistent
	private boolean isPublic   = false;
	// REST access restriction only !!!
	@DbPersistent
	private boolean isWritable = false;
	
	@DbPersistent(ro=true)
	@Public
	@DbIndex({"t3","t1","v0","v1","v2","v3","v4","v5","v6","v7","v8","v9"})
	private String type;
	@DbPersistent
	@Public
	@DbIndex({"v0"})
	private String value0;
	@DbPersistent
	@Public
	@DbIndex({"v1"})
	private String value1;
	@DbPersistent
	@Public
	@DbIndex({"v2"})
	private String value2;
	@DbPersistent
	@Public
	@DbIndex({"v3"})
	private String value3;
	@DbPersistent
	@Public
	@DbIndex({"v4"})
	private String value4;
	@DbPersistent
	@Public
	@DbIndex({"v5"})
	private String value5;
	@DbPersistent
	@Public
	@DbIndex({"v6"})
	private String value6;
	@DbPersistent
	@Public
	@DbIndex({"v7"})
	private String value7;
	@DbPersistent
	@Public
	@DbIndex({"v8"})
	private String value8;
	@DbPersistent
	@Public
	@DbIndex({"v9"})
	private String value9;
	@DbPersistent
	@Public
	@DbIndex({"t2"})
	private Date due;
	@DbPersistent
	@Public
	@DbIndex({"t1","v0","v1","v2","v3","v4","v5","v6","v7","v8","v9"})
	private boolean archived;
	@DbPersistent
	@Public
	@DbIndex({"t4"})
	private String foreignId;
	@DbPersistent
	@Public
	private Date foreignDate;
	@DbPersistent
	@Public
	private String status;

	@DbPersistent()
	@Public
	private MProperties data;

	@DbPersistent
	private Date lastSync;

	@DbPersistent
	@Public(readable=false,writable=false)
	private Date lastSyncTry;

	@DbPersistent(size=100,features=AttributeFeatureCut.NAME)
	@Public(readable=false,writable=false)
	private String lastSyncMsg;
	
	
	public SopData() {}
	
	public SopData(SopFoundation found, String type) {
		this(found.getId(), type);
	}

	public SopData(UUID found, String type) {
		this.type = type;
		this.foundation = found;
	}

	@Override
	public UUID getFoundation() {
		return foundation;
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		if (getFoundation() == null) return null;
		return MApi.lookup(FoundationApi.class).getFoundation(getFoundation());
	}

	public boolean isIsPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isIsWritable() {
		return isWritable;
	}

	public void setWritable(boolean isWritable) {
		this.isWritable = isWritable;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String string1) {
		this.value1 = string1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String string2) {
		this.value2 = string2;
	}

	public String getValue3() {
		return value3;
	}

	public void setValue3(String string3) {
		this.value3 = string3;
	}

	public String getValue4() {
		return value4;
	}

	public void setValue4(String string4) {
		this.value4 = string4;
	}

	public String getValue5() {
		return value5;
	}

	public void setValue5(String string5) {
		this.value5 = string5;
	}

	public String getType() {
		return type;
	}

	public synchronized MProperties getData() {
		if (data == null) data = new MProperties();
		return data;
	}

	public Date getDue() {
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public String getForeignId() {
		return foreignId;
	}

	public void setForeignId(String foreignId) {
		this.foreignId = foreignId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public Date getLastSyncTry() {
		return lastSyncTry;
	}

	public void setLastSyncTry(boolean save) {
		this.lastSyncTry = new Date();
		if (save && isAdbPersistent())
			try {
				save();
			} catch (MException e) {
				log().e(this,e);
			}
	}

	public String getLastSyncMsg() {
		return lastSyncMsg;
	}

	public void setLastSyncMsg(String lastSyncMsg, boolean save) {
		this.lastSyncMsg = lastSyncMsg;
		if (save && isAdbPersistent())
			try {
				save();
			} catch (MException e) {
				log().e(this,e);
			}
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, getId(), type, value0, value1, value2,value3,value4,value5,value6,value7,value8,value9,archived,foreignId,status);
	}

	public Date getForeignDate() {
		return foreignDate;
	}

	public void setForeignDate(Date foreignDate) {
		this.foreignDate = foreignDate;
	}

	public String getValue6() {
		return value6;
	}

	public void setValue6(String value6) {
		this.value6 = value6;
	}

	public String getValue7() {
		return value7;
	}

	public void setValue7(String value7) {
		this.value7 = value7;
	}

	public String getValue8() {
		return value8;
	}

	public void setValue8(String value8) {
		this.value8 = value8;
	}

	public String getValue9() {
		return value9;
	}

	public void setValue9(String value9) {
		this.value9 = value9;
	}

	public String getValue0() {
		return value0;
	}

	public void setValue0(String value0) {
		this.value0 = value0;
	}

}
