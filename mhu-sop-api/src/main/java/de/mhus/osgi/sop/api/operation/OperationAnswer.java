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
package de.mhus.osgi.sop.api.operation;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public class OperationAnswer {

    private String description;

    public OperationAnswer() {}

    public OperationAnswer(String path, Version version, IProperties properties) {
        description =
                path + (version == null ? "" : ":" + version) + "?" + MUri.implode(properties);
    }

    public OperationAnswer(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public String getPath() {
        int p = description.indexOf('?');
        if (p < 0) return description;
        String out = description.substring(0, p);
        p = out.indexOf(':');
        if (p < 0) return out;
        return out.substring(0, p);
    }

    public VersionRange getVersionRange() {
        String out = description;
        int p = out.indexOf('?');
        if (p >= 0) out = out.substring(0, p);
        p = out.indexOf(':');
        if (p < 0) return null;
        return new VersionRange(out.substring(p + 1));
    }

    public MProperties getProperties() {
        int p = description.indexOf('?');
        if (p < 0) return new MProperties();
        return new MProperties(MUri.explode(description.substring(p + 1)));
    }

    public OperationResult send(IProperties properties, String... executeOptions)
            throws NotFoundException {
        OperationApi api = M.l(OperationApi.class);
        MProperties prop = getProperties();
        if (properties != null) prop.putAll(properties);
        OperationResult res =
                api.doExecute(getPath(), getVersionRange(), null, prop, executeOptions);
        return res;
    }
}
