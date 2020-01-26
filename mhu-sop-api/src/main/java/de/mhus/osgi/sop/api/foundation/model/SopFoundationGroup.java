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
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbPrimaryKey;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.basics.consts.Identifier.TYPE;
import de.mhus.lib.errors.MException;

@GenerateConst(
        annotation = {DbPersistent.class, DbPrimaryKey.class},
        shortcuts = TYPE.FIELD)
public class SopFoundationGroup extends DbMetadata {

    @DbPersistent private String name;

    public SopFoundationGroup() {}

    public SopFoundationGroup(String name) {
        this.name = name;
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        return null;
    }

    public String getName() {
        return name;
    }
}
