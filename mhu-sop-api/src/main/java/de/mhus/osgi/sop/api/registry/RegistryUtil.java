package de.mhus.osgi.sop.api.registry;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.cfg.CfgLong;

public class RegistryUtil {

    public static final CfgLong CFG_WAIT_FOR_OTHERS = new CfgLong(RegistryApi.class, "waitForOthers", 200);
    public static final String MUTEX_PATH = RegistryApi.PATH_SYSTEM + "/mutex/";
    public static final String MASTER_VARNAME = "@master";
    public static final String VALUE_VARNAME = "@value";

    /**
     * Negotiate with other notes which one is the master of the resource.
     * 
     * @param path
     * @return true if my instance is the master.
     */
    public static boolean master(String path) {
        return master(path, 0) != null;
    }

    public static RegistryValue master(String path, long timeout) {
        timeout = Math.max(60000, timeout);
        RegistryApi rapi = M.l(RegistryApi.class);
        String p = MUTEX_PATH + path + MASTER_VARNAME;
        RegistryValue param = rapi.getParameter(p);
        if (param == null) {
            rapi.setParameter(p, "", timeout, false, false, false);
            param = rapi.getParameter(p);
            MThread.sleep(CFG_WAIT_FOR_OTHERS.value());
            param = rapi.getParameter(p);
        }
        return param.getSource().equals(rapi.getServerIdent()) ? param : null;
    }

    public static RegistryValue masterRefresh(String path, long timeout) {
        timeout = Math.max(60000, timeout);
        RegistryApi rapi = M.l(RegistryApi.class);
        String p = MUTEX_PATH + path + MASTER_VARNAME;
        RegistryValue param = rapi.getParameter(p);
        if (param == null) {
            rapi.setParameter(p, "", timeout, false, false, false);
            MThread.sleep(CFG_WAIT_FOR_OTHERS.value());
            param = rapi.getParameter(p);
        } else
        if (param.getTTL() < timeout / 2) {
            rapi.setParameter(p, "", timeout, false, false, false);
            param = rapi.getParameter(p);
        }
        return param;
    }

    public static boolean masterRemove(String name) {
        return M.l(RegistryApi.class).removeParameter(RegistryUtil.MUTEX_PATH + name + RegistryUtil.MASTER_VARNAME);
    }

    public static void setValue(String path, String value) {
        RegistryApi rapi = M.l(RegistryApi.class);
        String p = MUTEX_PATH + path + VALUE_VARNAME;
        rapi.setParameter(p, value, 1000, false, false, false);
    }

}
