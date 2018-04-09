package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.errors.MException;

public interface ModifyAuthorizationApi {

	/**
	 * Create or update resource ACL.
	 * 
	 * @param resName
	 * @param acl
	 * @throws MException
	 */
	void createAuthorization(String resName, String acl) throws MException;
	
	/**
	 * Delete resource ACL.
	 * 
	 * @param resName
	 * @throws MException
	 */
	void deleteAuthorization(String resName) throws MException;
	
}
