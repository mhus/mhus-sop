package de.mhus.osgi.sop.impl.cluster;

import java.util.function.BiConsumer;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.cluster.ClusterApi;
import de.mhus.osgi.sop.api.registry.RegistryUtil;

@Component(immediate=true)
public class RegistryClusterApiImpl extends MLog implements ClusterApi {

    private static CfgString CFG_PATH = new CfgString(ClusterApi.class, "registryPath", "cluster");
    public static CfgLong CFG_LOCK_TIMEOUT = new CfgLong(ClusterApi.class, "lockTimeout", MPeriod.HOUR_IN_MILLISECOUNDS);
    public static CfgLong CFG_LOCK_SLEEP = new CfgLong(ClusterApi.class, "lockSleep", 10000);

    @Override
    public Lock getLock(String name) {
        return M.l(LockManager.class).getLock(CFG_PATH.value() + "/" + name, n -> {return new RegistryLock(n);});
    }

    @Override
    public boolean isMaster(String name) {
        String p = MFile.normalizePath(CFG_PATH.value() + "/" + name);
        boolean master = RegistryUtil.master(p);
        return master;
    }

    @Override
    public String getStackName() {
        return MSystem.getHostname();
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
