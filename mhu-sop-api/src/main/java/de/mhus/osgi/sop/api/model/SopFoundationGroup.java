package de.mhus.osgi.sop.api.model;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.errors.MException;

public class SopFoundationGroup extends DbMetadata {

	@DbPersistent
	private String name;
	
	public SopFoundationGroup() {}
	
	public SopFoundationGroup(String name) {
		this.name = name;
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	public String getName() {
		return name;
	}

}
