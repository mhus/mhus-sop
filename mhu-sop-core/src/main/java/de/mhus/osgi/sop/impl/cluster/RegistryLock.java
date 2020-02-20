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
    private RegistryValue remoteLock;
    private Thread localLock = null;
    private long cnt = 0;
    private String stacktrace;

    public RegistryLock(String name) {
        this.name = name;
    }

    @Override
    public Lock lock() {
        synchronized (this) {
            if (isMyLocalLock()) return this;
            while (localLock != null) {
                MThread.sleep(ClusterApiViaRegistry.CFG_LOCK_SLEEP.value());
            }
            // get local lock
            localLock = Thread.currentThread();
            // get remote lock
            while (true) {
                remoteLock = RegistryUtil.master(name, ClusterApiViaRegistry.CFG_LOCK_TIMEOUT.value());
                if (remoteLock != null) {
                    lockTime = System.currentTimeMillis();
                    stacktrace =
                            MCast.toString(
                                    "RegistryLock " + Thread.currentThread().getId(),
                                    Thread.currentThread().getStackTrace());
                    if (!validateLock()) {
                        localLock = null;
                        remoteLock = null;
                        lockTime = 0;
                        throw new WrongStateException("already locked", name); // should not happen
                    }
                    cnt++;
                    return this;
                }
                MThread.sleep(ClusterApiViaRegistry.CFG_LOCK_SLEEP.value());
            }
        }
    }

    @Override
    public boolean lock(long timeout) {
        synchronized (this) {
            long start = System.currentTimeMillis();
            while (true) {
                if (localLock == null) {
                    // get local lock
                    localLock = Thread.currentThread();
                    continue; // to avoid timeout before remote lock is allocated
                } else {
                    // get remote lock
                    remoteLock = RegistryUtil.master(name, ClusterApiViaRegistry.CFG_LOCK_TIMEOUT.value());
                    if (remoteLock != null) {
                        lockTime = System.currentTimeMillis();
                        stacktrace =
                                MCast.toString(
                                        "RegistryLock " + Thread.currentThread().getId(),
                                        Thread.currentThread().getStackTrace());
                        if (!validateLock()) {
                            localLock = null;
                            remoteLock = null;
                            lockTime = 0;
                            throw new WrongStateException(
                                    "already locked", name); // should not happen
                        }
                        cnt++;
                        return true;
                    }
                }
                if (System.currentTimeMillis() - start >= timeout) {
                    localLock = null;
                    remoteLock = null;
                    lockTime = 0;
                    return false;
                }
                MThread.sleep(ClusterApiViaRegistry.CFG_LOCK_SLEEP.value());
            }
        }
    }

    public boolean validateLock() {
        if (!ClusterApiViaRegistry.CFG_LOCK_VALIDATE.value()) return true;
        MProperties properties = new MProperties();
        properties.setString("name", getName());
        try {

            OperationsSelector selector = new OperationsSelector();
            selector.setFilter(RegisterLockOperation.class.getCanonicalName());
            selector.addSelector(SelectorProvider.NOT_LOCAL_SELECTOR);
            List<OperationResult> results = selector.doExecuteAll(properties);

            for (OperationResult res : results)
                if (res.isSuccessful() && res.getReturnCode() == 1) {
                    log().w("Lock already given", name, res.getResult());
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
        if (remoteLock == null && localLock == null) return false;
        Thread l = localLock;
        if (l != null && l.getId() != Thread.currentThread().getId()) return false;
        boolean ret = true;
        if (remoteLock != null) {
            if (extend <= 0) ret = M.l(RegistryApi.class).removeParameter(remoteLock.getPath());
            else ret = RegistryUtil.masterExtend(name, extend) != null;
            remoteLock = null;
        }
        lockTime = 0;
        stacktrace = null;
        localLock = null;
        return ret;
    }

    @Override
    public void unlockHard() {
        if (remoteLock != null) M.l(RegistryApi.class).removeParameter(remoteLock.getPath());
        remoteLock = null;
        stacktrace = null;
        lockTime = 0;
        localLock = null;
    }

    public boolean isMyLocalLock() {
        return localLock == Thread.currentThread();
    }

    public boolean isMyLock() {
        return remoteLock != null || localLock == Thread.currentThread();
    }

    public boolean isRemoteLocked() {
        return remoteLock != null;
    }

    public boolean isLocalLocked() {
        return localLock != null;
    }

    @Override
    public boolean isLocked() {
        if (remoteLock != null || localLock != null) return true;
        return RegistryUtil.getMaster(name) != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOwner() {
        return (remoteLock == null ? "" : remoteLock.toString())
                + "::"
                + (localLock == null ? "" : localLock.getName() + " " + localLock.getId());
    }

    @Override
    public long getLockTime() {
        return lockTime;
    }

    @Override
    public boolean refresh() {
        synchronized (this) {
            if (remoteLock == null) return false;
            remoteLock = RegistryUtil.masterRefresh(name, ClusterApiViaRegistry.CFG_LOCK_TIMEOUT.value());
            if (remoteLock == null) {
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

    @Override
    public long getCnt() {
        return cnt;
    }

    @Override
    public String getStartStackTrace() {
        return stacktrace;
    }
}
