package de.mhus.osgi.sop.api.model;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.basics.AclControlled;
import de.mhus.lib.core.security.Ace;
import de.mhus.lib.errors.MException;

public class SopAcl extends DbMetadata implements AclControlled {
	
	@DbPersistent(type=TYPE.BLOB)
	private String list;
	@DbPersistent
	@DbIndex("u1")
	private String target;
	
	public SopAcl() {}
	
	public SopAcl(String target, String list) {
		this.target = target;
		this.list = list;
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	@Override
	public String getAcl() {
		return "*=" + Ace.RIGHTS_RO;
	}

	public String getList() {
		return list;
	}

	public String getTarget() {
		return target;
	}

}
