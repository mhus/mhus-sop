package de.mhus.osgi.sop.api.cluster;

import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.osgi.api.scheduler.SchedulerServiceAdapter;

public abstract class SchedulerClusterServiceAdapter extends SchedulerServiceAdapter {

    private TimerTaskInterceptor interceptor;
    private boolean stack = true;

    @Override
    public synchronized TimerTaskInterceptor getInterceptor() {
        if (interceptor == null) {
            interceptor = new TimerTaskClusterInterceptor(stack );
        }
        return interceptor;
    }

    public boolean isStack() {
        return stack;
    }

    protected void setStack(boolean stack) {
        this.stack = stack;
    }

}
