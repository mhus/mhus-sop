package de.mhus.osgi.sop.api.model;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.errors.MException;

public class FoundationGroup extends DbMetadata {

	@DbPersistent
	private String name;
	
	public FoundationGroup() {}
	
	public FoundationGroup(String name) {
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
