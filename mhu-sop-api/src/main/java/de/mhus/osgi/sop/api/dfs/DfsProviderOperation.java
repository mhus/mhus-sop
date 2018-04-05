package de.mhus.osgi.sop.api.dfs;

import java.io.IOException;
import java.util.Map;

import de.mhus.lib.core.util.MUri;

public interface DfsProviderOperation {

	String PARAM_SCHEME = "scheme";

	public FileInfo getFileInfo(MUri uri); 

	public MUri provideFile(MUri uri) throws IOException;

	public Map<String, MUri> getDirectoryList(MUri uri);

}
