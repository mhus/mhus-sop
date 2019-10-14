package de.mhus.osgi.sop.impl.cluster;

import java.util.List;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.concurrent.LockWithExtend;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.WrongStateException;
import de.mhus.osgi.sop.api.operation.OperationsSelector;
import de.mhus.osgi.sop.api.operation.SelectorProvider;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryUtil;
import de.mhus.osgi.sop.api.registry.RegistryValue;

public class RegistryLock extends MLog implements LockWithExtend {

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
            while (localLock != null) {
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
            // get local lock
            localLock = Thread.currentThread();
            // get remote lock
            while (true) {
                lock = RegistryUtil.master(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
                if ( lock != null) {
                    lockTime = System.currentTimeMillis();
                    if (!validateLock()) {
                        localLock = null;
                        lock = null;
                        lockTime = 0;
                        throw new WrongStateException("already locked",name); // should not happen
                    }
                    return this;
                }
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
        }
    }

    @Override
    public boolean lock(long timeout) {
        synchronized (this) {
            if (isMyLock()) return true;

            long start = System.currentTimeMillis();
            while (true) {
                if (localLock == null) {
                    // get local lock
                    if (!isMyLock())
                        localLock = Thread.currentThread();
                } else {
                    // get remote lock
                    lock = RegistryUtil.master(name, RegistryClusterApiImpl.CFG_LOCK_TIMEOUT.value());
                    if (lock != null) {
                        lockTime = System.currentTimeMillis();
                        if (!validateLock()) {
                            localLock = null;
                            lock = null;
                            lockTime = 0;
                            throw new WrongStateException("already locked",name); // should not happen
                        }
                        return true;
                    }
                }
                if (System.currentTimeMillis() - start >= timeout ) return false;
                MThread.sleep(RegistryClusterApiImpl.CFG_LOCK_SLEEP.value());
            }
        }
    }

    public boolean validateLock() {
        if(!RegistryClusterApiImpl.CFG_LOCK_VALIDATE.value()) return true;
        MProperties properties = new MProperties();
        properties.setString("name", getName());
        try {
            
            OperationsSelector selector = new OperationsSelector();
            selector.setFilter(RegisterLockOperation.class.getCanonicalName());
            selector.addSelector(SelectorProvider.NOT_LOCAL_SELECTOR);
            List<OperationResult> results = selector.doExecuteAll(properties);
            
            for (OperationResult res : results)
                if (res.isSuccessful() && res.getReturnCode() == 1) {
                    log().w("Lock already given",name, res.getResult());
                    return false;
                }
        } catch (NotFoundException e) {
        }
        return true;
    }


    @Override
    public boolean unlock() {
        return unlock(0);
    }

    @Override
    public boolean unlock(long extend) {
            if (lock == null && localLock == null) return false;
            Thread l = localLock;
            if (l != null && l.getId() != Thread.currentThread().getId()) return false;
            boolean ret = true;
            if (lock != null) {
                if (extend <= 0)
                    ret = M.l(RegistryApi.class).removeParameter(lock.getPath());
                else
                    ret = RegistryUtil.masterExtend(name, extend) != null;
                lock = null;
            }
            localLock = null;
            lockTime = 0;
            return ret;
    }

    @Override
    public void unlockHard() {
            RegistryUtil.masterRemove(name);
            lock = null;
            localLock = null;
            lockTime = 0;
    }

    public boolean isMyLock() {
        return lock != null || localLock != null;
    }

    @Override
    public boolean isLocked() {
        if (lock != null || localLock != null) return true;
        return RegistryUtil.getMaster(name) != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOwner() {
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

    @Override
    public String toString() {
        return MSystem.toString(this, name, isMyLock(), isLocked());
    }
}
