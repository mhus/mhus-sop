package de.mhus.osgi.sop.impl.cluster;

import java.util.HashMap;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.AbstractOperation;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.core.strategy.TaskContext;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.definition.FmText;
import de.mhus.osgi.sop.api.cluster.ClusterApi;

@Component(service=Operation.class, property="tags=acl=*")
public class RegisterLockOperation extends AbstractOperation {

    @SuppressWarnings("unchecked")
    @Override
    protected OperationResult doExecute2(TaskContext context) throws Exception {
        String name = context.getParameters().getString("name");
        Lock lock = M.l(ClusterApi.class).getLock(name);
        Successful ret = new Successful(this, "ok", "name", name);
        if (lock.isLocked())
            ((HashMap<Object, Object>)ret.getResult()).put("owner",lock.getOwner());
        ret.setReturnCode(lock.isLocked() ? 1 : 0);
        return ret;
    }

    @Override
    protected OperationDescription createDescription() {
        return new OperationDescription(getUuid(),this.getClass(), this, Version.V_1_0_0, "Is register lock, returns 0 for no 1 for yes", new DefRoot(
                new FmText("name","Name","Name of the lock to check")
                ));
    }

}
