package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.concurrent.LockWithExtend;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.lib.core.strategy.DefaultTaskContext;

public class TimerTaskClusterInterceptor implements TimerTaskInterceptor {

    private String name;
    private Lock mutex;
    private boolean stack;
    
    public TimerTaskClusterInterceptor() {
        stack = true;
    }
    
    public TimerTaskClusterInterceptor(boolean stack) {
        this.stack = stack;
    }
    
    public TimerTaskClusterInterceptor(String name, boolean stack) {
        this.name = name;
        this.stack = stack;
    }
    
    @Override
    public void initialize(SchedulerJob job) {
        if (name == null)
            name = job.getName();
    }

    @Override
    public boolean beforeExecution(SchedulerJob job, DefaultTaskContext context, boolean forced) {
        if (mutex != null) {
            mutex.close();
            mutex = null;
        }
        if (!ClusterApi.CFG_ENABLED.value()) return true;
        ClusterApi api = M.l(ClusterApi.class);
        @SuppressWarnings("resource")
        Lock lock = stack ? api.getStackLock(name) : api.getLock(name);
        if (lock.isLocked()) return false;
        mutex = lock.lock();
        return true;
    }

    @Override
    public void afterExecution(SchedulerJob job, DefaultTaskContext context) {
        if (mutex != null) {
            if (mutex instanceof LockWithExtend)
                // extends lock 10 sec. before next execution
                ((LockWithExtend)mutex).unlock(job.getNextExecutionTime() - System.currentTimeMillis() - 10000);
            else
                mutex.unlock();
            mutex = null;
        }
    }

    @Override
    public void onError(SchedulerJob job, DefaultTaskContext context, Throwable e) {

    }

    public Lock getLock() {
        return mutex;
    }

}
