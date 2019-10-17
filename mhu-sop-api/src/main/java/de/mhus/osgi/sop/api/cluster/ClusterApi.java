package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.base.service.ServerIdent;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.concurrent.Lock;

public interface ClusterApi {

    public static final CfgBoolean CFG_ENABLED = new CfgBoolean(ClusterApi.class, "enabled", true); // XXX set to false
    
    /**
     * Get the lock object for a named resource.
     * @param name
     * @return The lock for the resource
     */
    Lock getLock(String name);

    /**
     * Get a lock object for the named resource of the current service (hostname).
     * @param name
     * @return The lock for the resource
     */
    default Lock getServiceLock(String name) {
        return getLock(getServiceName() + "/" + name);
    }

    /**
     * Check if I'm the master of a named resource.
     * The method will update masters before return.
     * @param name
     * @return true if I'm the master
     */
    boolean isMaster(String name);
    
    /**
     * Check if I'm the master of a named resource of the current service (hostname).
     * The method will update masters before return.
     * @param name
     * @return true if I'm the master
     */
    default boolean isServiceMaster(String name) {
        return isMaster(getServiceName() + "/" + name);
    }
    
    void registerListener(String name, ValueListener consumer );
    
    default void registerServiceListener(String name, ValueListener consumer ) {
        registerListener(getServiceName() + "/" + name, consumer);
    }
    
    void fireEvent(String name, String value);
    
    default void fireServiceEvent(String name, String value) {
        fireEvent(getServiceName() + "/" + name, value);
    }

    void unregisterListener(ValueListener consumer);
    
    default String getServiceName() {
        return M.l(ServerIdent.class).getService();
    }

}
