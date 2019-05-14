package de.mhus.osgi.sop.impl.aaa.util;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLdap;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.crypt.MCrypt;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.errors.NotSupportedException;

public class AccountFromLdap extends MLog implements AccountSource {

    public static final SearchControls SEARCH_CONTROLS_ALL = MLdap.getSimpleSearchControls();
    public static final SearchControls SEARCH_CONTROLS_EMPTY = MLdap.getSimpleSearchControls();
    static {
        SEARCH_CONTROLS_EMPTY.setReturningAttributes(new String[0]);
    }
    public static final Date STARTED_DATE = new Date();
    
    private String url;
    private String principal;
    private String password;
    private String userSearchName;
    private String userSearchFilter;
    private MProperties userAttributeMapping;
    private String userAttributesDisplayName;
    private String userAttributesActive;
    private String userAttributesUuid;

    private String groupsSearchName;
    private String groupsSearchFilter;

    @Override
    public Account findAccount(String account) {

        LdapAccount ret = new LdapAccount(account);
        if (!ret.reloadAccount()) return null;
        
        return ret;
    }

    @Override
    public ModifyAccountApi getModifyApi() {
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserSearchName() {
        return userSearchName;
    }

    public void setUserSearchName(String userSearchName) {
        this.userSearchName = userSearchName;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public void setUserAttributeMapping(String mapping) {
        userAttributeMapping = MProperties.explodeToMProperties(mapping);
    }
    
    public String getUserAttributesDisplayName() {
        return userAttributesDisplayName;
    }

    public void setUserAttributesDisplayName(String userAttributesDisplayName) {
        this.userAttributesDisplayName = userAttributesDisplayName;
    }

    public String getGroupsSearchName() {
        return groupsSearchName;
    }

    public void setGroupsSearchName(String groupsSearchName) {
        this.groupsSearchName = groupsSearchName;
    }

    public String getGroupsSearchFilter() {
        return groupsSearchFilter;
    }

    public void setGroupsSearchFilter(String groupsSearchFilter) {
        this.groupsSearchFilter = groupsSearchFilter;
    }

    public String getUserAttributesActive() {
        return userAttributesActive;
    }

    public void setUserAttributesActive(String userAttributesActive) {
        this.userAttributesActive = userAttributesActive;
    }

    public String getUserAttributesUuid() {
        return userAttributesUuid;
    }

    public void setUserAttributesUuid(String userAttributesUuid) {
        this.userAttributesUuid = userAttributesUuid;
    }

    private class LdapAccount implements Account {

        private MProperties params;
        private String account;
        private String displayName;
        private HashSet<String> groups;
        private boolean active;
        private UUID uuid;

        public LdapAccount(String account) {
            this.account = account;
        }

        @Override
        public boolean hasGroup(String group) {
            return groups.contains(group.toUpperCase());
        }

        @Override
        public String getName() {
            return account;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean validatePassword(String password) {
            try {
                DirContext ctx = MLdap.getConnection(url, account, MPassword.decode(password));
                ctx.close();
            } catch (Throwable t) {
                log().e(account,t);
            }
            return false;
        }

        @Override
        public boolean isSynthetic() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public IReadProperties getAttributes() {
            return params;
        }

        @Override
        public void putAttributes(IReadProperties properties) throws NotSupportedException {
            throw new NotSupportedException();
        }

        @Override
        public String[] getGroups() throws NotSupportedException {
            return groups.toArray(new String[groups.size()]);
        }

        @Override
        public boolean reloadAccount() {
            try {
                DirContext ctx = MLdap.getConnection(url, principal, MPassword.decode(password));
                
                // user parameters
                NamingEnumeration<SearchResult> res = ctx.search( 
                        userSearchName.replace("%u", account),
                        userSearchFilter.replace("%u", account),
                        SEARCH_CONTROLS_ALL);
                Map<String, Object> first = MLdap.getFirst(res);
                String fqdn = String.valueOf(first.get(MLdap.KEY_NAME_NAMESPACE));
                params = new MProperties();
                if (userAttributeMapping != null) {
                    for (Entry<String, Object> mapping : userAttributeMapping.entrySet()) {
                        params.put(mapping.getKey(), first.get(mapping.getValue()));
                    }
                } else {
                    params.putAll(first);
                }
                
                displayName = userAttributesDisplayName == null ? account : StringCompiler.compile(userAttributesDisplayName).execute(first);
                displayName = displayName.trim();
                
                active = userAttributesActive == null ? true : MCast.toboolean(first.get(userAttributesActive), false);
                
                uuid = userAttributesUuid == null ? MCrypt.toUuidHash(fqdn) : UUID.fromString(String.valueOf(first.get(userAttributesUuid)));

                // groups
                res = ctx.search( 
                        getGroupsSearchName().replace("%u", account).replace("%fqdn", fqdn),
                        getGroupsSearchFilter().replace("%u", account).replace("%fqdn", fqdn),
                        SEARCH_CONTROLS_EMPTY);
                
                List<String> groupsList = MLdap.getNames(res);
                groupsList.replaceAll(o -> MString.afterIndex(o, '=').toUpperCase());
                groups = new HashSet<>();
                groups.addAll(groupsList);
                
                res.close();
                
                ctx.close();
                return true;
            } catch (Throwable t) {
                log().e(account, t);
            }
            return false;
        }

        @Override
        public Date getCreationDate() {
            return STARTED_DATE;
        }

        @Override
        public Date getModifyDate() {
            return STARTED_DATE;
        }

        @Override
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean isActive() {
            return active;
        }
        
    }
}
