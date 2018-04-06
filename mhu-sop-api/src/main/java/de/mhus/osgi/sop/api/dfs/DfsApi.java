package de.mhus.osgi.sop.api.dfs;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import de.mhus.lib.core.util.MUri;

public interface DfsApi {

	String SCHEME_DFQ =  "dfq";

	/**
	 * Search and return a file in the file space.
	 * @param uri
	 * @return Info or null
	 */
	default FileInfo getFileInfo(String uri) {
		return getFileInfo(MUri.toUri(uri));
	}

	/**
	 * Search and return a file in the file space.
	 * @param uri
	 * @return Info or null
	 */
	FileInfo getFileInfo(MUri uri);
	
	/**
	 * Request and return a file queue handle to the prepared file.
	 * 
	 * @param uri Uri to the requested file
	 * @return File queue handle
	 */
	MUri exportFile(MUri uri);

	/**
	 * Request a list of directory entries (if supported by the
	 * remote dfs). If the entry is a sub directory it will end with an slash.
	 * 
	 * @param uri
	 * @return a list of entries and the corresponding URI
	 */
	Map<String, MUri> getDirectoryList(MUri uri);

	Collection<String> listProviders();

	void importFile(MUri queueUri, MUri target) throws IOException;

	void deleteFile(MUri uri) throws IOException;

	void createDirecories(MUri uri) throws IOException;

}
