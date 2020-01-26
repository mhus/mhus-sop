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
package de.mhus.osgi.sop.api.registry;

import de.mhus.lib.core.MLog;

public class RegistryPathControlAdapter extends MLog implements RegistryPathControl {

    @Override
    public boolean isTakeControl(String path) {
        return true;
    }

    @Override
    public RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value) {
        return value;
    }

    @Override
    public boolean checkRemoveParameter(RegistryManager manager, RegistryValue value) {
        return true;
    }

    @Override
    public RegistryValue checkSetParameterFromRemote(RegistryManager manager, RegistryValue value) {
        return value;
    }

    @Override
    public boolean checkRemoveParameterFromRemote(RegistryManager manager, RegistryValue value) {
        return true;
    }
}
