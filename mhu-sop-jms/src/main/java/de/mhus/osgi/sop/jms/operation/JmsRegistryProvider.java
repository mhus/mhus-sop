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
package de.mhus.osgi.sop.jms.operation;

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.osgi.sop.api.registry.RegistryProvider;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(immediate = true)
public class JmsRegistryProvider extends MLog implements RegistryProvider {

    public static final CfgBoolean CFG_ENABLED =
            new CfgBoolean(JmsRegistryProvider.class, "enabled", true);
    public static final CfgLong CFG_SYNCHRONIZE_WAIT =
            new CfgLong(
                    JmsRegistryProvider.class,
                    "synchronizeWait",
                    MPeriod.MINUTE_IN_MILLISECOUNDS * 3);

    @Override
    public boolean publish(RegistryValue entry) {
        if (!CFG_ENABLED.value()) return false;
        return JmsApiImpl.instance.registryPublish(entry);
    }

    @Override
    public boolean remove(String path) {
        if (!CFG_ENABLED.value()) return false;
        return JmsApiImpl.instance.registryRemove(path);
    }

    @Override
    public boolean publishAll() {
        if (!CFG_ENABLED.value()) return false;
        return JmsApiImpl.instance.sendLocalRegistry();
    }

    @Override
    public boolean requestAll() {
        if (!CFG_ENABLED.value()) return false;
        return JmsApiImpl.instance.requestRegistry();
    }

    @Override
    public boolean isReady() {
        return 
                JmsApiImpl.instance != null 
                && 
                JmsApiImpl.instance.isConnected() 
                && 
                JmsApiImpl.instance.register != null
                &&
                JmsApiImpl.instance.register.size() > 0 // should not be empty
                ;
    }
}
