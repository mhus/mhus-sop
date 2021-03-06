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

import java.util.Collection;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public interface OperationsProvider {

    void collectOperations(
            List<OperationDescriptor> list,
            String filter,
            VersionRange version,
            Collection<String> providedTags);

    OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException;

    OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IProperties properties,
            String... executeOptions)
            throws NotFoundException;

    OperationResult doExecute(
            OperationDescriptor desc, IProperties properties, String... executeOptions)
            throws NotFoundException;

    void synchronize();
}
