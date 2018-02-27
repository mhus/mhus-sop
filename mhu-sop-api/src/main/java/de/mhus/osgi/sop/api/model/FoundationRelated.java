package de.mhus.osgi.sop.api.model;

import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SopApi;

public interface FoundationRelated {

	UUID getFoundation();
	
	default public DbMetadata findParentObject() throws MException {
		return MApi.lookup(SopApi.class).getFoundation(getFoundation());
	}

}
