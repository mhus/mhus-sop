package de.mhus.osgi.sop.test;

import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import de.mhus.lib.core.MLdap;
import de.mhus.osgi.sop.impl.aaa.util.AccountFromLdap;

@SuppressWarnings("unused")
public class TryLdapAccount {

    public static void main(String[] args) throws NamingException {
        AccountFromLdap provider = new AccountFromLdap();
        
        
        DirContext ctx = MLdap.getConnection("ldap://localhost:11389", "cn=Directory Manager", "nein");
        
//        NamingEnumeration<SearchResult> res = ctx.search("ou=people,dc=ngnetwork,dc=de", MLdap.FILTER_ALL_CLASSES,MLdap.getSimpleSearchControls());
//        for (Map<String, Object> entry : MLdap.iterate(res)) {
//            System.out.println(entry);
//        }

//      NamingEnumeration<SearchResult> res = ctx.search("ou=groups,dc=ngnetwork,dc=de", MLdap.FILTER_ALL_CLASSES,MLdap.getSimpleSearchControls());
//      for (Map<String, Object> entry : MLdap.iterate(res)) {
//          System.out.println(entry);
//      }

//        SearchControls sc = MLdap.getSimpleSearchControls();
//        sc.setReturningAttributes(new String[] {});
//        NamingEnumeration<SearchResult> res = ctx.search(
//                "ou=groups,dc=ngnetwork,dc=de", 
//                "(uniqueMember=uid=adm-hummel,ou=people,dc=ngnetwork,dc=de)",
//                sc);
//        for (Map<String, Object> entry : MLdap.iterate(res)) {
//            System.out.println(entry);
//        }

        NamingEnumeration<SearchResult> res = ctx.search("ou=people,dc=ngnetwork,dc=de", "(uid=adm-hummel)",MLdap.getSimpleSearchControls());
        for (Map<String, Object> entry : MLdap.iterate(res)) {
            for (Entry<String, Object> kv : entry.entrySet())
            System.out.println(kv.getKey() + "=" + kv.getValue());
        }

        res.close();
        
//        provider.setUrl("ldap://localhost:11389");
//        provider.setPrincipal("cn=Directory Manager");
//        provider.setPassword("nein");
//        
//        provider.setUserSearchBase("dc=ngnetwork,dc=de");
//        provider.setUserAttributeNames("*");
//        provider.setPrincipalDomain("(uid=%u)");
//        
//        Account acc = provider.findAccount("hfo-hummel");
//        
//        System.out.println(acc.getAttributes());
    }
}
