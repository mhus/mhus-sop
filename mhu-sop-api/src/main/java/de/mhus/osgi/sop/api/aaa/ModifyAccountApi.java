package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.errors.MException;

public interface ModifyAccountApi {

	void createAccount(String username, String password, IReadProperties properties) throws MException;
	
	void deleteAccount(String username) throws MException;
	
	void changePassword(String username, String newPassword) throws MException;
	
	void changeAccount(String username, IReadProperties properties) throws MException;
	
}
