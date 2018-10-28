package de.mhus.osgi.sop.api.model;

import java.util.HashSet;
import java.util.Set;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;

public class SopAccount extends DbMetadata implements Account {

	@DbIndex("u1")
	@DbPersistent(ro=true)
	private String name;
	@DbPersistent
	private String password;
	@DbPersistent
	private HashSet<String> groups;
	@DbPersistent
	private MProperties attributes;
	
	public SopAccount() {}
	
	public SopAccount(String name, String pass, IReadProperties properties) {
		this.name = name;
		groups = new HashSet<>();
		this.attributes = new MProperties(properties);
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Set<String> groups() {
		return groups;
	}
	
	@Override
	public boolean hasGroup(String group) {
		return groups.contains(group);
	}

	@Override
	public boolean isValid() {
		return isAdbPersistent();
	}

	@Override
	public boolean validatePassword(String password) {
		return MPassword.validatePasswordMD5(password, this.password);
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return attributes.getString(MConstants.ADDR_DISPLAY_NAME, name);
	}

	@Override
	public IReadProperties getAttributes() {
		return attributes;
	}

	@Override
	public void putAttributes(IReadProperties properties) throws NotSupportedException {
		attributes.putReadProperties(properties);
	}

	@Override
	public String[] getGroups() throws NotSupportedException {
		return groups.toArray(new String[groups.size()]);
	}

	@Override
	public boolean reloadAccount() {
		try {
			reload();
			return true;
		} catch (MException e) {
			log().w(this,e);
			return false;
		}
	}

	@Override
	public String toString() {
		return MSystem.toString(this, name);
	}

	public void setPassword(String newPassword) {
		password = MPassword.encodePasswordMD5(newPassword);
	}
	
	public void setPasswordInternal(String newPassword) {
		password = newPassword;
	}
	
}
