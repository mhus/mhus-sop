package de.mhus.osgi.sop.impl.cluster;

import java.util.HashMap;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MEventHandler;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.cluster.ClusterApi;
import de.mhus.osgi.sop.api.cluster.LockListener;
import de.mhus.osgi.sop.api.cluster.ValueListener;
import de.mhus.osgi.sop.api.registry.RegistryUtil;

public class ClusterApiViaRegistry extends MLog implements ClusterApi {

    private static CfgString CFG_PATH = new CfgString(ClusterApi.class, "registryPath", "cluster");
    public static CfgLong CFG_LOCK_TIMEOUT =
            new CfgLong(ClusterApi.class, "lockTimeout", MPeriod.HOUR_IN_MILLISECOUNDS);
    public static CfgLong CFG_LOCK_SLEEP = new CfgLong(ClusterApi.class, "lockSleep", 200);
    public static CfgBoolean CFG_LOCK_VALIDATE =
            new CfgBoolean(ClusterApi.class, "lockValidate", false);

    HashMap<String, ValueEventHandler> valueListeners = new HashMap<>();
    LockEventHandler lockListeners = new LockEventHandler();
    private static ClusterApiViaRegistry instance;

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    public static ClusterApiViaRegistry instance() {
        return instance;
    }

    @Override
    public Lock getLock(String name) {
        return M.l(LockManager.class)
                .getLock(
                        CFG_PATH.value() + "/" + name,
                        n -> {
                            return new RegistryLock(n);
                        });
    }

    @Override
    public boolean isMaster(String name) {
        String p = MFile.normalizePath(CFG_PATH.value() + "/" + name);
        boolean master = RegistryUtil.master(p);
        return master;
    }

    private ValueEventHandler getEventHandler(String name) {
        ValueEventHandler ret = valueListeners.get(name);
        if (ret == null) {
            ret = new ValueEventHandler();
            valueListeners.put(name, ret);
        }
        return ret;
    }

    @Override
    public void registerValueListener(String name, ValueListener consumer) {
        synchronized (valueListeners) {
            ValueEventHandler handler = getEventHandler(name);
            handler.registerWeak(consumer);
        }
    }

    @Override
    public void fireValueEvent(String name, String value) {
        String p = MFile.normalizePath(CFG_PATH.value() + "/" + name);
        RegistryUtil.setValue(p, value);
    }

    public void fireValueEventLocal(String name, String value, boolean local) {
        ValueEventHandler handler = null;
        synchronized (valueListeners) {
            name = name.substring(CFG_PATH.value().length() + 1);
            handler = getEventHandler(name);
        }
        handler.fire(name, value, local);
    }

    public void fireLockEventLocal(LockListener.EVENT event, String name, boolean local) {
        ValueEventHandler handler = null;
        synchronized (valueListeners) {
            name = name.substring(CFG_PATH.value().length() + 1);
            handler = getEventHandler(name);
        }
        handler.fire(event, name, local);
    }

    static class ValueEventHandler extends MEventHandler<ValueListener> {
        @Override
        public void onFire(ValueListener listener, Object event, Object... values) {
            listener.event((String) event, (String) values[0], (Boolean) values[1]);
        }
    }

    static class LockEventHandler extends MEventHandler<LockListener> {
        @Override
        public void onFire(LockListener listener, Object event, Object... values) {
            listener.event((LockListener.EVENT) event, (String) values[0], (Boolean) values[1]);
        }
    }

    @Override
    public void unregisterValueListener(ValueListener consumer) {
        synchronized (valueListeners) {
            for (ValueEventHandler handler : valueListeners.values()) {
                handler.unregister(consumer);
            }
        }
    }

    @Override
    public void registerLockListener(LockListener consumer) {
        lockListeners.registerWeak(consumer);
    }

    @Override
    public void unregisterLockListener(LockListener consumer) {
        lockListeners.unregister(consumer);
    }
}
