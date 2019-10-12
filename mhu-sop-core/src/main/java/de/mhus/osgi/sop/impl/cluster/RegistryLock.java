package de.mhus.osgi.sop.impl.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryUtil;
import de.mhus.osgi.sop.api.registry.RegistryValue;

public class RegistryLock implements Lock {

    private String name;
    protected long lockTime = 0;
    private RegistryValue lock;
    private Thread localLock = null;

    public RegistryLock(String name) {
        this.name = name;
    }

    @Override
    public Lock lock() {
        synchronized (this) {
            if (isLocked()) {
                refresh();
                return this;
            }
            // get local lock
            localLock = Thread.currentThread();
            // get remote lock
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
            if (localLock != null && isLocked()) return true;

            long start = System.currentTimeMillis();
            while (true) {
                if (localLock == null) {
                    // get local lock
                    if (!isLocked())
                        localLock = Thread.currentThread();
                } else {
                    // get remote lock
                    lock = RegistryUtil.master(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
                    if (lock != null) {
                        lockTime = System.currentTimeMillis();
                        return true;
                    }
                }
                if (System.currentTimeMillis() - start >= timeout ) return false;
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
        }
    }

    @Override
    public boolean unlock() {
//        synchronized (this) {
            if (lock == null && localLock == null) return false;
            localLock = null;
            lockTime = 0;
            if (lock == null) return true;
            boolean ret = M.l(RegistryApi.class).removeParameter(lock.getPath());
            lock = null;
            return ret;
//        }
    }

    @Override
    public void unlockHard() {
//        synchronized (this) {
            RegistryUtil.masterRemove(name);
            lock = null;
            localLock = null;
            lockTime = 0;
//        }
    }

    @Override
    public boolean isLocked() {
        return lock != null || localLock != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocker() {
        return  (lock == null ? "" : lock.toString() + "\n") + 
                (localLock == null ? "" : MCast.toString(localLock.toString(),localLock.getStackTrace()));
    }

    @Override
    public long getLockTime() {
        return lockTime;
    }

    @Override
    public boolean refresh() {
        synchronized (this) {
            if (lock == null) return false;
            lock = RegistryUtil.masterRefresh(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
            if (lock == null) {
                lockTime = 0;
                return false;
            }
        }
        return true;
    }

}
