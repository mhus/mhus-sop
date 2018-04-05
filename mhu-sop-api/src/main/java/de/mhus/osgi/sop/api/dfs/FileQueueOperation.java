package de.mhus.osgi.sop.api.dfs;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public interface FileQueueOperation {

	File getFile(UUID id);

	FileInfo getFileInfo(UUID id);

	Set<UUID> getQueuedIdList();
	
}
