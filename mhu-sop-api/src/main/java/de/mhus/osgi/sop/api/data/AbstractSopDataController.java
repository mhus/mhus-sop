/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.api.data;

import java.util.Date;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.foundation.model.SopData;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.rest.CallContext;

public abstract class AbstractSopDataController extends MLog implements SopDataController {

    protected long syncTimeout = MPeriod.MINUTE_IN_MILLISECOUNDS * 5;

    @Override
    public void createSopData(SopData data) throws Exception {
        data.save();
    }

    @Override
    public void updateSopData(SopData data) throws Exception {
        data.save();
    }

    @Override
    public void deleteSopData(SopData data) throws Exception {
        data.delete();
    }

    @Override
    public boolean isNeedSync(SopData obj) {
        //		Date ls = obj.getLastSync();
        //		if (ls != null && !MTimeInterval.isTimeOut(ls.getTime(), getSyncTimeout()))
        //			return false;
        Date ls = obj.getLastSyncTry();
        if (ls != null && !MPeriod.isTimeOut(ls.getTime(), getSyncTimeout())) return false;
        return true;
    }

    public long getSyncTimeout() {
        return syncTimeout;
    }

    @Override
    public void doPrepareForOutput(SopData obj, CallContext context, boolean listMode)
            throws MException {
        obj.setDbHandler(null); // do not save anymore

        obj.getData().entrySet().removeIf((e) -> MSystem.isPasswordName(e.getKey()));

        //		Iterator<Entry<String, Object>> iter = obj.getData().iterator();
        //		while (iter.hasNext()) {
        //			Entry<String, Object> next = iter.next();
        //			if (next.getKey().contains("pass"))
        //				iter.remove();
        //		}
    }

    @Override
    public void syncListBeforeLoad(
            SopFoundation found,
            String type,
            String search,
            Boolean archived,
            Date due,
            List<SopData> list) {}

    @Override
    public OperationResult actionSopData(SopData data, String action, IProperties parameters)
            throws Exception {
        return new NotSuccessful("", "action not found", 502);
    }

    @Override
    public OperationResult actionSopDataOperation(SopData data, String action, IProperties p)
            throws Exception {
        return new NotSuccessful("", "action not found", 502);
    }
}
