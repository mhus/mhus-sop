package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.concurrent.Lock;

public class ClusterApiDummy implements ClusterApi {

    @Override
    public Lock getLock(String name) {
        return M.l(LockManager.class).getLock("cluster_" + name);
    }

    @Override
    public boolean isReady() {
        return true;
    }

}
