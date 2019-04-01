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
package de.mhus.osgi.sop.vaadin.desktop;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.security.AccessControl;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public class VaadinSopAccessControl extends MLog implements AccessControl {

	private static final String ATTR_TICKET = "_access_ticket";
	private static final String ATTR_NAME = "_access_name";
	private VaadinSession session;
	private AccessApi aaa;

	public VaadinSopAccessControl() {
		session = UI.getCurrent().getSession();
		aaa = MApi.lookup(AccessApi.class);
	}
	
	@Override
	public boolean hasGroup(String role) {
		Account acc = getAccount();
		if (acc == null) return false;
		return acc.hasGroup(role);
	}

	@Override
	public String getName() {
		String ret = (String)session.getAttribute(ATTR_NAME);
		if (ret == null) return "?";
		return ret;
	}

	@Override
	public boolean signIn(String username, String password) {
		
		String ticket = aaa.createUserTicket(username, password);
		try {
			AaaContext context = aaa.process(ticket, session.getLocale());
			aaa.release(context);
			session.setAttribute(ATTR_TICKET, ticket);
			session.setAttribute(ATTR_NAME, username);
			return true;
		} catch (Throwable t) {
			log().w(username,t);
			return false;
		}
	}

	@Override
	public boolean isUserSignedIn() {
		return session.getAttribute(ATTR_TICKET) != null;
	}

	@Override
	public void signOut() {
		session.setAttribute(ATTR_NAME, null);
		session.setAttribute(ATTR_TICKET, null);
	}

	@Override
	public Account getAccount() {
		String account = (String)session.getAttribute(ATTR_NAME);
		if (account == null) return null;
		try {
			return aaa.getAccount(account);
		} catch (MException e) {
			log().w(account,e);
			return null;
		}
	}

	public static String getUserName(VaadinSession session) {
		String ret = (String)session.getAttribute(ATTR_NAME);
		if (ret == null) return "?";
		return ret;
	}

	public static Account getUserAccount(VaadinSession session) {
		String account = (String)session.getAttribute(ATTR_NAME);
		if (account == null) return null;
		try {
			AccessApi aaa = MApi.lookup(AccessApi.class);
			return aaa.getAccount(account);
		} catch (MException e) {
			MLogUtil.log().w(account, e);
			return null;
		}
	}

}
