package de.mhus.osgi.sop.impl.dfs;

import java.util.Date;

import de.mhus.lib.core.M;
import de.mhus.lib.core.util.MUri;
import de.mhus.osgi.sop.api.dfs.FileInfo;

public class FileInfoImpl implements FileInfo {

	private static final long serialVersionUID = 1L;
	protected String name;
	protected long size;
	protected long modified;
	private String uri;

	public FileInfoImpl(MUri uri, String name, long size, long modified) {
		this.uri = uri.toString();
		this.name = name;
		this.size = size;
		this.modified = modified;
	}
	
	public FileInfoImpl(MUri uri) {
		this.uri = uri.toString();
		name = uri.getPath();
		String[] params = uri.getParams();
		if (params != null) {
			size     = M.c(params[0], 0);
			modified = M.c(params[1], new Date()).getTime();
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getModified() {
		return modified;
	}

	@Override
	public String getUri() {
		return uri;
	}

}
