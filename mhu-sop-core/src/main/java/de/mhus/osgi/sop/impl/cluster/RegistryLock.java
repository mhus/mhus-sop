package de.mhus.osgi.sop.impl.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryUtil;
import de.mhus.osgi.sop.api.registry.RegistryValue;

public class RegistryLock implements Lock {

    private String name;
    protected long lockTime = 0;
    private RegistryValue lock;

    public RegistryLock(String name) {
        this.name = name;
    }

    @Override
    public Lock lock() {
        synchronized (this) {
            while (true) {
                lock = RegistryUtil.master(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
                if ( lock != null) {
                    lockTime = System.currentTimeMillis();
                    return this;
                }
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
        }
    }

    @Override
    public boolean lock(long timeout) {
        synchronized (this) {
            long start = System.currentTimeMillis();
            while (true) {
                lock = RegistryUtil.master(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
                if (lock != null) {
                    lockTime = System.currentTimeMillis();
                    return true;
                }
                if (System.currentTimeMillis() - start >= timeout ) return false;
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
        }
    }

    @Override
    public boolean unlock() {
        synchronized (this) {
            if (lock == null) return false;
            boolean ret = M.l(RegistryApi.class).removeParameter(lock.getPath());
            lock = null;
            lockTime = 0;
            return ret;
        }
    }

    @Override
    public void unlockHard() {
        synchronized (this) {
            RegistryUtil.masterRemove(name);
            lock = null;
            lockTime = 0;
        }
    }

    @Override
    public boolean isLocked() {
        return lock != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocker() {
        return lock == null ? null : lock.toString();
    }

    @Override
    public boolean isPrivacy() {
        return false;
    }

    @Override
    public long getLockTime() {
        return lockTime;
    }

}
