package de.mhus.osgi.sop.impl.cluster;

import java.util.HashMap;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MEventHandler;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.sop.api.cluster.ClusterApi;
import de.mhus.osgi.sop.api.cluster.ValueListener;
import de.mhus.osgi.sop.api.registry.RegistryUtil;

@Component(immediate=true)
public class RegistryClusterApiImpl extends MLog implements ClusterApi {

    private static CfgString CFG_PATH = new CfgString(ClusterApi.class, "registryPath", "cluster");
    public static CfgLong CFG_LOCK_TIMEOUT = new CfgLong(ClusterApi.class, "lockTimeout", MPeriod.HOUR_IN_MILLISECOUNDS);
    public static CfgLong CFG_LOCK_SLEEP = new CfgLong(ClusterApi.class, "lockSleep", 1000);

    HashMap<String,EventHandler> listeners = new HashMap<>();
    private static RegistryClusterApiImpl instance;

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }
    
    public static RegistryClusterApiImpl instance() {
        return instance;
    }

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

    private EventHandler getEventHandler(String name) {
        EventHandler ret = listeners.get(name);
        if (ret == null) {
            ret = new EventHandler();
            listeners.put(name, ret);
        }
        return ret;
    }
    
    @Override
    public void registerListener(String name, ValueListener consumer) {
        synchronized (listeners) {
            EventHandler handler = getEventHandler(name);
            handler.registerWeak(consumer);
        }
    }

    @Override
    public void fireEvent(String name, String value) {
        String p = MFile.normalizePath(CFG_PATH.value() + "/" + name);
        RegistryUtil.setValue(p, value);
    }
    
    public void fireEventLocal(String name, String value) {
        EventHandler handler = null;
        synchronized (listeners) {
            name = name.substring(CFG_PATH.value().length() + 1);
            handler = getEventHandler(name);
        }
        handler.fire(name, value);
    }

    static class EventHandler extends MEventHandler<ValueListener> {
        @Override
        public void onFire(ValueListener listener, Object event, Object ... values) {
            listener.event((String)event, (String)values[0]);
        }
    }

    @Override
    public void unregisterListener(ValueListener consumer) {
        synchronized (listeners) {
            for (EventHandler handler : listeners.values()) {
                handler.unregister(consumer);
            }
        }
    }

}
