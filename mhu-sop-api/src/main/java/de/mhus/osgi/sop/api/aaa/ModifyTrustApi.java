package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.errors.MException;

public interface ModifyTrustApi {

	/**
	 * Create a Trust configuration.
	 * 
	 * @param name
	 * @param password
	 * @param properties
	 * @throws MException
	 */
	void createTrust(String name, String password, IReadProperties properties) throws MException;
	
	/**
	 * Delete Trust configuration.
	 * 
	 * @param name
	 * @throws MException
	 */
	void deleteTrust(String name) throws MException;
	
	/**
	 * Change Trust secret.
	 * 
	 * @param name
	 * @param newPassword
	 * @throws MException
	 */
	void changePassword(String name, String newPassword) throws MException;
	
	/**
	 * Change Trust properties.
	 * 
	 * @param name
	 * @param properties
	 * @throws MException
	 */
	void changeTrust(String name, IReadProperties properties) throws MException;

}
