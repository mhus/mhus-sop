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

import java.io.IOException;
import java.util.Set;

public interface RegistryApi {

    public static final String PATH_SYSTEM =
            "/system/"; // this path will not fire events to the cfg system
    public static final String PATH_LOCAL = "/local/"; // this path will not be published
    public static final String PATH_WORKER = "/worker/";

    public static final String SOURCE_LOCAL = "@local@";

    /**
     * Return the value information of a node parameter.
     *
     * @param path Node path and parameter name e.g. '/node1/node2@parameter'
     * @return The value or null if not found
     */
    RegistryValue getParameter(String path);

    /**
     * Returns a list of children nodes.
     *
     * @param path Path to the node where to request the children.
     * @return List of node names or an empty list.
     */
    Set<String> getNodeChildren(String path);

    /**
     * Returns a list of parameters of the given node.
     *
     * @param path Path to the node where to request the parameters.
     * @return List of parameter names.
     */
    Set<String> getParameterNames(String path);

    /**
     * Set and publish a value for the given parameter.
     *
     * @param path Node path and parameter name e.g. '/node1/node2@parameter'
     * @param value New value
     * @param timeout Timeout in milliseconds after receive or 0
     * @param readOnly Readonly can only be updated by the source
     * @param persistent Set the possibility that the value will be stored (currently only local)
     * @param local Overwrite other remote values
     * @return true if the value was really changed or false if the value was the same as before
     */
    boolean setParameter(
            String path,
            String value,
            long timeout,
            boolean readOnly,
            boolean persistent,
            boolean local);

    default boolean setParameter(String path, String value) {
        return setParameter(path, value, 0, true, false, false);
    }

    /**
     * Remove a node or parameter. And publish the information.
     *
     * @param path
     * @return true if was removed
     */
    boolean removeParameter(String path);

    Set<RegistryValue> getParameters(String path);

    boolean publishAll();

    boolean requestAll();

    void save() throws IOException;

    void load();

    String getServerIdent();

    /**
     * Return true if the api is ready. The state can change multiple times while lifetime and depends on
     * active services.
     * @return true if ready.
     */
    boolean isReady();
}
