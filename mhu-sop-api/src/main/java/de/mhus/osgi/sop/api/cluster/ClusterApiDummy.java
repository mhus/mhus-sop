package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.concurrent.Lock;

public class ClusterApiDummy implements ClusterApi {

    @Override
    public Lock getLock(String name) {
        return M.l(LockManager.class).getLock("cluster_" + name);
    }

    @Override
    public boolean isMaster(String name) {
        return true;
    }

    @Override
    public void registerValueListener(String name, ValueListener consumer) {
        
    }

    @Override
    public void fireValueEvent(String name, String value) {
        
    }

    @Override
    public void unregisterValueListener(ValueListener consumer) {
        
    }

    @Override
    public void registerLockListener(LockListener consumer) {
        
    }

    @Override
    public void unregisterLockListener(LockListener consumer) {
        
    }

    @Override
    public boolean isReady() {
        return true;
    }

}
