package de.mhus.osgi.sop.api.dfs;

import java.io.Serializable;

public interface FileInfo extends Serializable {
	
	/**
	 * The technical name of the file.
	 * 
	 * @return The name
	 */
	String getName();

	/**
	 * Size of the file.
	 * 
	 * @return Size
	 */
	long getSize();

	/**
	 * Modify date.
	 * @return modified
	 */
	long getModified();

	/**
	 * File queue uri.
	 * @return reference id
	 */
	String getUri();
	
}
