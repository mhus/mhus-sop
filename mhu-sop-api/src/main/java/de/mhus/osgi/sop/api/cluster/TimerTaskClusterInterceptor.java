package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.errors.MRuntimeException;

public class TimerTaskClusterInterceptor implements TimerTaskInterceptor {

    private String name;
    private Lock mutex;
    
    @Override
    public void initialize(SchedulerJob job) {
        if (name != null) throw new MRuntimeException("interceptor already attached",name);
        name = job.getName();
    }

    @Override
    public boolean beforeExecution(SchedulerJob job, DefaultTaskContext context, boolean forced) {
        if (mutex != null) {
            mutex.close();
            mutex = null;
        }
        if (!ClusterApi.CFG_ENABLED.value()) return true;
        Lock lock = M.l(ClusterApi.class).getLock(name);
        if (lock.isLocked()) return false;
        mutex = lock.lock();
        return true;
    }

    @Override
    public void afterExecution(SchedulerJob job, DefaultTaskContext context) {
        if (mutex != null) {
            mutex.close();
            mutex = null;
        }
    }

    @Override
    public void onError(SchedulerJob job, DefaultTaskContext context, Throwable e) {

    }

}
