package de.mhus.osgi.sop.impl.dfs;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.services.scheduler.SchedulerService;
import de.mhus.osgi.services.scheduler.SchedulerServiceAdapter;

@Component(provide=SchedulerService.class,immediate=true,properties="interval=*/15 * * * * *")
public class FileQueueTimer extends SchedulerServiceAdapter {

	@Override
	public void run(Object environment) {
		try {
			if (FileQueueApiImpl.instance != null)
				FileQueueApiImpl.instance.cleanupQueue();
		} catch (Throwable t) {
			log().e(t);
		}
	}

}
