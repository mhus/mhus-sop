package de.mhus.osgi.sop.impl.cluster;

import java.util.function.BiConsumer;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.cluster.ClusterApi;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryValue;

public class RegistryClusterApiImpl extends MLog implements ClusterApi {

    private static CfgString CFG_PATH = new CfgString(ClusterApi.class, "registryPath", RegistryApi.PATH_SYSTEM + "/master/cluster");

    @Override
    public Lock getLock(String name) {
        RegistryApi rapi = M.l(RegistryApi.class);
        String p = CFG_PATH + "/" + name + "@seed";
        RegistryValue param = rapi.getParameter(p);
        if (param == null) {
//XXX            rapi.setParameter(p, value)
        }
        return null;
    }

    @Override
    public boolean isMaster(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getStackName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerListener(String name, BiConsumer<String, String> consumer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fireEvent(String name, String value) {
        // TODO Auto-generated method stub
        
    }

}
