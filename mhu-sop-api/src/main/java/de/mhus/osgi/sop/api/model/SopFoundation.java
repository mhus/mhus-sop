package de.mhus.osgi.sop.api.model;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;

public class SopFoundation extends DbMetadata {

	@DbPersistent
	private String group = "";
	@DbPersistent
	private String ident;
	
	public SopFoundation() {}
	
	public SopFoundation(String ident, String group) {
		super();
		this.ident = ident;
		this.group = group;
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		return MApi.lookup(SopApi.class).getFoundationGroup(group);
	}

	public String getGroup() {
		return group;
	}

	public String getIdent() {
		return ident;
	}

}
