package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.concurrent.Lock;

public class ClusterApiDummy implements ClusterApi {

    @Override
    public Lock getLock(String name) {
        return null;
    }

    @Override
    public boolean isMaster(String name) {
        return false;
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

}
