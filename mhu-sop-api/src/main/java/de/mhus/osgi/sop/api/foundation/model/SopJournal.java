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
package de.mhus.osgi.sop.api.foundation.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.model.AttributeFeatureCut;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbPrimaryKey;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.basics.consts.Identifier.TYPE;
import de.mhus.lib.core.M;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.FoundationRelated;

@GenerateConst(
        annotation = {DbPersistent.class, DbPrimaryKey.class},
        shortcuts = TYPE.FIELD)
public class SopJournal extends DbMetadata implements FoundationRelated {

    public static final String QUEUE_BPM = "bpm";

    @DbPersistent(ro = true)
    @DbIndex({"u1", "3"})
    private UUID foundation;

    @DbPersistent(ro = true)
    @DbIndex({"u1", "2"})
    private long order;

    @DbPersistent(ro = true)
    private String createdBy;

    @DbPersistent(size = 10, ro = true)
    @DbIndex({"u1"})
    private String queue;

    @DbPersistent(ro = true, features = AttributeFeatureCut.NAME)
    private String event;

    @DbPersistent(ro = true)
    private HashMap<String, String> data;

    public SopJournal() {}

    public SopJournal(UUID foundation, String queue, String event, long order, String... data) {
        this.foundation = foundation;
        this.queue = queue;
        this.event = event;
        this.order = order;
        this.data = new HashMap<>();
        createdBy = AaaUtil.currentAccount().getName();
        if (data != null)
            for (int i = 0; i < data.length - 1; i += 2) this.data.put(data[i], data[i + 1]);
    }

    public SopJournal(
            UUID foundation, String queue, String event, long order, Map<String, Object> data) {
        this(foundation, queue, event, order);
        this.data = new HashMap<>();
        for (Entry<String, Object> entry : data.entrySet())
            try {
                this.data.put(entry.getKey(), String.valueOf(entry.getValue()));
            } catch (Throwable t) {
                log().e(t);
            }
    }

    public String getQueue() {
        return queue;
    }

    public String getEvent() {
        return event;
    }

    public Map<String, String> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }

    public long getOrder() {
        return order;
    }

    @Override
    public UUID getFoundation() {
        return foundation;
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        return M.l(FoundationApi.class).getFoundation(getFoundation());
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
