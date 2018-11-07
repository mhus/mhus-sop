package de.mhus.osgi.sop.impl.aaa.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopAccount;

public class AccountFromDb extends MLog implements AccountSource, ModifyAccountApi {

	@Override
	public void createAccount(String username, String password, IReadProperties properties) throws MException {
		Account acc = findAccount(username);
		if (acc != null) throw new MException("Account already exists",username);
		XdbService db = MApi.lookup(SopApi.class).getManager();
		acc = db.inject(new SopAccount(username, password, properties)); 
		((SopAccount)acc).save();
	}

	@Override
	public void deleteAccount(String username) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		((SopAccount)acc).delete();
	}

	@Override
	public void changePasswordInternal(String username, String newPassword) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		((SopAccount)acc).setPasswordInternal(newPassword);
		((SopAccount)acc).save();
	}
	
	@Override
	public void changePassword(String username, String newPassword) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		((SopAccount)acc).setPassword(newPassword);
		((SopAccount)acc).save();
	}

	@Override
	public void changeAccount(String username, IReadProperties properties) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		((SopAccount)acc).clearAttributes();
		((SopAccount)acc).putAttributes(properties);
		((SopAccount)acc).save();
	}

	@Override
	public void appendGroups(String username, String... groups) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		for (String group : groups)
			((SopAccount)acc).groups().add(group);
		((SopAccount)acc).save();
	}

	@Override
	public void removeGroups(String username, String... groups) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		for (String group : groups)
			((SopAccount)acc).groups().remove(group);
		((SopAccount)acc).save();
	}

	@Override
	public Collection<String> getGroups(String username) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		return Collections.unmodifiableCollection( ((SopAccount)acc).groups() );
	}

	@Override
	public Collection<String> getAccountList(String filter) {
		filter = filter.replace('%', ' ').trim();
		XdbService db = MApi.lookup(SopApi.class).getManager();
		AQuery<SopAccount> query = Db.query(SopAccount.class);
		boolean not = false;
		if (filter.startsWith("!")) {
			not = true;
			filter = filter.substring(1).trim();
		}
		if (filter.equals("*")) {
			// nothing
		} else
		if (filter.startsWith("*") && filter.endsWith("*")) {
			if (not)
				query.not(Db.like("name", "%" + filter + "%"));
			else
				query.like("name", "%" + filter + "%");
		} else
		if (filter.startsWith("*")) {
			if (not)
				query.not(Db.like("name", "%" + filter));
			else
				query.like("name", "%" + filter);
		} else
		if (filter.endsWith("*")) {
			if (not)
				query.not(Db.like("name", filter + "%"));
			else
				query.like("name", filter + "%");
		}
		LinkedList<String> out = new LinkedList<>();
		try {
			DbCollection<SopAccount> res = db.getByQualification(query);
			for (SopAccount entry : res)
				out.add(entry.getName());
		} catch(Exception e) {
			log().e(filter,e);
		}
		return out;
	}
	
	@Override
	public Account findAccount(String account) {
		XdbService db = MApi.lookup(SopApi.class).getManager();
		try {
			return db.getObjectByQualification(Db.query(SopAccount.class).eq("name", account));
		} catch (Exception e) {
			log().e(account,e);
		}
		return null;
	}

	@Override
	public ModifyAccountApi getModifyApi() {
		return this;
	}

	@Override
	public void activateAccount(String username, boolean active) throws MException {
		Account acc = findAccount(username);
		if (acc == null) throw new MException("Account not found",username);
		((SopAccount)acc).setActive(active);
		((SopAccount)acc).save();
	}

}
