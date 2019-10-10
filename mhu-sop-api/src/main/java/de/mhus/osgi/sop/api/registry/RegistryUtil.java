package de.mhus.osgi.sop.api.registry;

import de.mhus.lib.core.M;

public class RegistryUtil {

    public static final String UNIQUE_PATH = RegistryApi.PATH_SYSTEM + "/master/";

    public static boolean unique(String path) {
        RegistryApi rapi = M.l(RegistryApi.class);
        String p = UNIQUE_PATH + path + "@seed";
        RegistryValue param = rapi.getParameter(p);
        if (param == null) {
            rapi.setParameter(p, "");
            param = rapi.getParameter(p);
        }
        return param.getSource().equals(rapi.getServerIdent());
    }
}
