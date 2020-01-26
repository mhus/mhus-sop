package de.mhus.osgi.sop.api.cluster;

public interface LockListener {

    enum EVENT {
        LOCK,
        UNLOCK
    };

    void event(EVENT event, String lock, boolean local);
}
