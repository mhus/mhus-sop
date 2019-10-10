package de.mhus.osgi.sop.api.cluster;

import java.util.function.BiConsumer;

import de.mhus.lib.core.concurrent.Lock;

public interface ClusterApi {

    /**
     * Get the lock object for a named resource.
     * @param name
     * @return The lock for the resource
     */
    Lock getLock(String name);

    /**
     * Get a lock object for the named resource of the current stack (hostname).
     * @param name
     * @return The lock for the resource
     */
    default Lock getStackLock(String name) {
        return getLock(getStackName() + "." + name);
    }

    /**
     * Check if I'm the master of a named resource.
     * The method will update masters before return.
     * @param name
     * @return true if I'm the master
     */
    boolean isMaster(String name);
    
    /**
     * Check if I'm the master of a named resource of the current stack (hostname).
     * The method will update masters before return.
     * @param name
     * @return true if I'm the master
     */
    default boolean isStackMaster(String name) {
        return isMaster(getStackName() + "." + name);
    }
    
    /**
     * Return the name of the current stack. By default the hostname.
     * @return technical stack name
     */
    String getStackName();
    
    void registerListener(String name, BiConsumer<String,String> consumer );
    
    default void registerStackListener(String name, BiConsumer<String,String> consumer ) {
        registerStackListener(getStackName() + "." + name, consumer);
    }
    
    void fireEvent(String name, String value);
    
    default void fireStackEvent(String name, String value) {
        fireEvent(getStackName() + "." + name, value);
    }
    
}
