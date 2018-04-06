package de.mhus.osgi.sop.api.dfs;

import java.io.IOException;
import java.util.Map;

import de.mhus.lib.core.util.MUri;

public interface DfsProviderOperation {

	String PARAM_SCHEME = "scheme";

	FileInfo getFileInfo(MUri uri); 

	MUri exportFile(MUri uri) throws IOException;

	Map<String, MUri> getDirectoryList(MUri uri);

	void importFile(MUri queueUri, MUri target) throws IOException;
	
	/**
	 * Delete file or directory.
	 * 
	 * @param uri
	 * @throws IOException
	 */
	void deleteFile(MUri uri) throws IOException;
	
	/**
	 * Create all the missing directories.
	 * 
	 * @param uri
	 * @throws IOException
	 */
	void createDirecories(MUri uri) throws IOException;
	
}
