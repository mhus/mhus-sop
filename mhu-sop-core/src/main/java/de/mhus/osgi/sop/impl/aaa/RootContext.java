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
package de.mhus.osgi.sop.impl.aaa;

import de.mhus.osgi.sop.impl.AaaContextImpl;

public class RootContext extends AaaContextImpl {

    private static final AccountRoot ROOT_ACCOUNT = new AccountRoot();

    public RootContext(AaaContextImpl parent) {
        this();
        setParent(parent);
    }

    public RootContext() {
        super(ROOT_ACCOUNT);
        adminMode = true;
    }

    public void setAdminMode(boolean admin) {
        adminMode = admin;
    }
}
