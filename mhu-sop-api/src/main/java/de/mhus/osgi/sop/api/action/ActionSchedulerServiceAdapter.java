package de.mhus.osgi.sop.api.action;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.api.scheduler.SchedulerServiceAdapter;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopActionTask;
import de.mhus.osgi.sop.api.model._SopActionTask;

public abstract class ActionSchedulerServiceAdapter extends SchedulerServiceAdapter {

    private ActionDescription desc;

    public ActionSchedulerServiceAdapter() {
        desc = getClass().getAnnotation(ActionDescription.class);
        if (desc == null)
            log().w("action description not set",getClass().getCanonicalName());
    }
    @Override
    public String getInterval() {
        String ret = super.getInterval();
        return ret == null ? "5min" : ret;
    }

    @Override
    public void run(Object environment) {
        // check if enabled
        try {
            if (!isEnabled()) return;
            // get supported action name
            String queueName = getQueueName();
            if (queueName == null) return;
            // get db manager
            SopApi api = M.l(SopApi.class);
            if (api == null) return;
            XdbService db = api.getManager();
            if (db == null) return;
            // create query
            AQuery<SopActionTask> query = Db.query(SopActionTask.class).eq(_SopActionTask._QUEUE, queueName);
            int limit = getRoundLimit();
            int processed = 0;
            boolean isStarted = false; // marker if a action is processed and doBegin() was called
            
            // iterate actions
            try (DbCollection<SopActionTask> res = db.getByQualification(query)) {
                for ( SopActionTask action : res) {
                    long timeToWait = getTimeToWait();
                    if (timeToWait <= 0 || MPeriod.isTimeOut(action.getModifyDate().getTime(), timeToWait)) {
                        log().d("execute",action);
                        if (!isStarted) {
                            doBegin();
                            isStarted = true;
                        }
                        try {
                            if (doExecute(action))
                                action.delete();
                            else
                                action.save(); // set new modify date
                        } catch (Throwable t) {
                            log().e("delete action because of an error",action,t);
                            action.delete();
                        }
                        
                        processed++;
                        if (limit > 0 && processed >= limit) break;
                        
                    }
                }
            }
            
            if (isStarted)
                doEnd();
            
        } catch (Throwable t) {
            log().e(t);
        }
        
    }

    /**
     * Called before the first action will be executed
     */
    protected abstract void doBegin();
    
    /**
     * Called after the last action for this block was executed
     */
    protected abstract void doEnd();
    
    /**
     * Overwrite to process the action. If the method throws an exception the action task will be deleted.
     * 
     * @param action The action to process
     * @return true if the action was processed and the task can be deleted.
     */
    protected abstract boolean doExecute(SopActionTask action);

    private long getTimeToWait() {
        if (desc == null) return 0;
        return desc.timeToWait();
    }
    protected int getRoundLimit() {
        if (desc == null) return 0;
        return desc.limit();
    }

    /**
     * Overwrite this if you need to disable the action processing depending on other then the timer resources.
     * @return true if action processing is enabled (default)
     */
    protected boolean isEnabled() {
        return true;
    }

    protected String getQueueName() {
        if (desc == null) return null;
        return desc.queue();
    }

}
