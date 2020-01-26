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

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.model.AttributeFeatureCut;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbPrimaryKey;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.basics.consts.Identifier.TYPE;
import de.mhus.lib.core.M;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.foundation.FoundationApi;

@GenerateConst(
        annotation = {DbPersistent.class, DbPrimaryKey.class},
        shortcuts = TYPE.FIELD)
public class SopFoundation extends DbMetadata {

    @DbPersistent private String group = "";
    @DbPersistent @Public private String ident;
    @DbPersistent private boolean active;

    @DbPersistent(features = AttributeFeatureCut.NAME)
    @Public
    private String title;

    public SopFoundation() {}

    public SopFoundation(String ident, String title, String group) {
        super();
        this.ident = ident;
        this.title = title;
        this.group = group;
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        return M.l(FoundationApi.class).getFoundationGroup(group);
    }

    public String getGroup() {
        return group;
    }

    public String getIdent() {
        return ident;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
