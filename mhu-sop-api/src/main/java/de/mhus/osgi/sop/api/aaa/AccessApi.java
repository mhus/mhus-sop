/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.api.aaa;

import java.util.Locale;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;

/**
 * Basic concept of authorization:
 * 
 * - The user/subject has a set of groups and a name.
 * - The object can be authorized by an acl set
 * - There are a set of ace's for every action
 * - check if the user has access by processing the ace entries
 * 
 * @author mikehummel
 *
 */
public interface AccessApi extends SApi {

	// access
	
	public static final String ROOT_NAME = "root";
	public static final String GUEST_NAME = "guest";
	
	AaaContext process(String ticket, Locale locale);
	AaaContext release(String ticket);
	AaaContext process(Account ac, Trust trust, boolean admin, Locale locale);
	AaaContext release(Account ac);
	AaaContext release(AaaContext context);
	void resetContext();
	
	AaaContext getCurrentOrGuest();
	
	Account getCurrenAccount() throws MException;
	
	Account getAccount(String account) throws MException;
	
	AaaContext processAdminSession();
	boolean validatePassword(Account account, String password);

	String createTrustTicket(String trustName, AaaContext user);

	/**
	 * Check if a resource access is granted to the account
	 * 
	 * Check if the account has access. The list is the rule set.
	 * Rules:
	 * - '*'access to all
	 * - 'user:' prefix to allow user
	 * - 'notuser:' prefix to deny user
	 * - 'notgrout:' prefix to deny group
	 * - group name to allow group
	 * 
	 * @param account
	 * @param acl 
	 * @param resourceName Name of the resource
	 * @param id The id of the object
	 * @param action The action to do or null for general access
	 * @param def 
	 * @return x
	 */
	boolean hasGroupAccess(Account account, String acl, String action, String def);
	
	boolean hasGroupAccess(Account account, Class<?> who, String acl, String action, String def);
	
	boolean hasResourceAccess(Account account, String resourceName, String id, String action, String def);
	String createUserTicket(String username, String password);
	AaaContext getGuestContext();
	
	void process(AaaContext context);
	
	/**
	 * Return the current context or null if not set.
	 * 
	 * @return Context or null
	 */
	AaaContext getCurrent();
	
	AccountGuest getGuestAccount();
	String getResourceAccessAcl(Account account, String resourceName, String id, String action, String def);
	String getGroupAccessAcl(Account account, String aclName, String action, String def);
	AaaContext processUserSession(String user, Locale locale);

}
