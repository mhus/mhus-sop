/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.api.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.basics.IsNull;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;

@GenerateConst
public class SopAccount extends DbMetadata implements Account {

    @DbIndex("u1")
    @DbPersistent(ro = true)
    private String name;

    @DbPersistent private String password;
    @DbPersistent private HashSet<String> groups;
    @DbPersistent private MProperties attributes;
    @DbPersistent private boolean active;

    public SopAccount() {}

    public SopAccount(String name, String pass, IReadProperties properties) {
        this.name = name;
        this.password = MPassword.encodePasswordMD5(pass);
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
        MProperties out = new MProperties(attributes);
        out.put("ro.created", getCreationDate());
        out.put("ro.modified", getModifyDate());
        out.put("ro.uuid", getId());
        return attributes;
    }

    public void clearAttributes() {
        attributes.clear();
    }

    @Override
    public void putAttributes(IReadProperties properties) throws NotSupportedException {
        for (Map.Entry<? extends String, ? extends Object> e : properties.entrySet())
            if (!e.getKey().startsWith("ro.")) {
                if (e.getValue() instanceof IsNull) attributes.remove(e.getKey());
                else attributes.put(e.getKey(), e.getValue());
            }
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
            log().w(this, e);
            return false;
        }
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name, active);
    }

    public void setPassword(String newPassword) {
        password = MPassword.encodePasswordMD5(newPassword);
    }

    public void setPasswordInternal(String newPassword) {
        password = newPassword;
    }

    @Override
    public UUID getUUID() {
        return getId();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
