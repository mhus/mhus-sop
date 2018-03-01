package de.mhus.osgi.sop.api.model;

import java.util.Date;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.model.AttributeFeatureCut;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;

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
	@DbIndex({"t1","t2"})
	private String type;
	@DbPersistent
	@Public
	@DbIndex({"t2"})
	private String value1;
	@DbPersistent
	@Public
	private String value2;
	@DbPersistent
	@Public
	private String value3;
	@DbPersistent
	@Public
	private String value4;
	@DbPersistent
	@Public
	private String value5;
	@DbPersistent
	@Public
	private Date due;
	@DbPersistent
	@Public
	private boolean archived;
	@DbPersistent
	@Public
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
	@Public(readable=false,writeable=false)
	private Date lastSyncTry;

	@DbPersistent(size=100,features=AttributeFeatureCut.NAME)
	@Public(readable=false,writeable=false)
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
		return MApi.lookup(SopApi.class).getFoundation(getFoundation());
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
		return MSystem.toString(this, getId(), type, value1, value2,value3,value4,value5,archived,foreignId,status);
	}

	public Date getForeignDate() {
		return foreignDate;
	}

	public void setForeignDate(Date foreignDate) {
		this.foreignDate = foreignDate;
	}

}
