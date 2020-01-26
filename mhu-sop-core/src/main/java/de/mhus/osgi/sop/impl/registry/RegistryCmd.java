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
package de.mhus.osgi.sop.impl.registry;

import java.util.Date;
import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryPathControl;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Command(scope = "sop", name = "registry", description = "Registry commands")
@Service
public class RegistryCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + " list [path]\n"
                            + " set/add <path> <value>\n"
                            + " remove <path>\n"
                            + " publish\n"
                            + " request\n"
                            + " save\n"
                            + " load\n"
                            + "",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "path",
            required = false,
            description = "Path to Node",
            multiValued = false)
    String path;

    @Argument(
            index = 2,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    @Option(
            name = "-t",
            aliases = "--timeout",
            description = "Set timeout for new entries",
            required = false)
    long timeout = 0;

    @Option(
            name = "-w",
            aliases = "--writeable",
            description = "Set writable by others",
            required = false)
    boolean writable = false;

    @Option(
            name = "-p",
            aliases = "--persistent",
            description = "Set value persistent (locally)",
            required = false)
    boolean persistent = false;

    @Option(name = "-l", aliases = "--local", description = "Local overwrite", required = false)
    boolean local = false;

    @Override
    public Object execute2() throws Exception {
        RegistryApi api = M.l(RegistryApi.class);

        if (cmd.equals("list")) {

            if (path != null && !path.endsWith("*")) {
                ConsoleTable out = new ConsoleTable(tblOpt);
                out.setHeaderValues(
                        "Path", "Value", "Source", "Updated", "TTL", "RO", "Persistent");
                for (String child : api.getNodeChildren(path))
                    out.addRowValues("/" + child, "[node]", "", "", "", "", "");
                for (RegistryValue value : api.getParameters(path))
                    out.addRowValues(
                            "@" + MString.afterIndex(value.getPath(), '@'),
                            value.getValue(),
                            value.getSource(),
                            new Date(value.getUpdated()),
                            value.getTimeout() > 0
                                    ? MPeriod.getIntervalAsString(value.getTTL())
                                    : "",
                            value.isReadOnly(),
                            value.isPersistent());
                out.print(System.out);
            } else {
                RegistryManager manager = M.l(RegistryManager.class);
                LinkedList<RegistryValue> list = new LinkedList<>(manager.getAll());
                list.sort(
                        (a, b) -> {
                            return a.getPath().compareTo(b.getPath());
                        });
                ConsoleTable out = new ConsoleTable(tblOpt);
                out.setHeaderValues(
                        "Path", "Value", "Source", "Updated", "TTL", "RO", "Persistent");
                for (RegistryValue value : list) {
                    if (path == null
                                    && !value.getPath().startsWith(RegistryApi.PATH_SYSTEM)
                                    && !value.getPath().startsWith(RegistryApi.PATH_WORKER)
                            || MString.compareFsLikePattern(value.getPath(), path))
                        out.addRowValues(
                                value.getPath(),
                                value.getValue(),
                                value.getSource(),
                                new Date(value.getUpdated()),
                                value.getTimeout() > 0
                                        ? MPeriod.getIntervalAsString(
                                                value.getTimeout()
                                                        - (System.currentTimeMillis()
                                                                - value.getUpdated()))
                                        : "",
                                value.isReadOnly(),
                                value.isPersistent());
                }
                out.print(System.out);
            }
        } else if (cmd.equals("get")) {
            RegistryValue entry = api.getParameter(path);
            System.out.println("Path      : " + entry.getPath());
            System.out.println("Source    : " + entry.getSource());
            System.out.println("Persistent: " + entry.isPersistent());
            System.out.println("Readonly  : " + entry.isReadOnly());
            System.out.println(
                    "Updated   : "
                            + MDate.toIsoDateTime(entry.getUpdated())
                            + " Age: "
                            + MPeriod.getIntervalAsString(
                                    System.currentTimeMillis() - entry.getUpdated()));
            System.out.println(
                    "Timeout   : "
                            + entry.getTimeout()
                            + " "
                            + (entry.getTimeout() > 0
                                    ? MPeriod.getIntervalAsString(
                                            entry.getTimeout()
                                                    - (System.currentTimeMillis()
                                                            - entry.getUpdated()))
                                    : ""));
            System.out.println("Value     : " + entry.getValue());
            RegistryValue c = entry.getRemoteValue();
            if (c != null) {
                System.out.println();
                System.out.println("Remote Path      : " + c.getPath());
                System.out.println("Remote Source    : " + c.getSource());
                System.out.println("Remote Persistent: " + c.isPersistent());
                System.out.println("Remote Readonly  : " + c.isReadOnly());
                System.out.println(
                        "Remote Updated   : "
                                + MDate.toIsoDateTime(c.getUpdated())
                                + " Age: "
                                + MPeriod.getIntervalAsString(
                                        System.currentTimeMillis() - c.getUpdated()));
                System.out.println(
                        "Remote Timeout   : "
                                + c.getTimeout()
                                + " "
                                + (c.getTimeout() > 0
                                        ? MPeriod.getIntervalAsString(
                                                c.getTimeout()
                                                        - (System.currentTimeMillis()
                                                                - c.getUpdated()))
                                        : ""));
                System.out.println("Remote Value     : " + c.getValue());
            }

            RegistryManager manager = M.l(RegistryManager.class);
            RegistryPathControl controller = manager.getPathController(path);
            if (controller != null) {
                System.out.println();
                System.out.println("Path Controller: " + controller);
            }
        } else if (cmd.equals("set") || cmd.equals("add")) {
            if (MString.isIndex(path, '@')) {
                if (api.setParameter(path, parameters[0], timeout, !writable, persistent, local))
                    System.out.println("SET");
                else System.out.println("NOT CHANGED");
            } else {
                for (int i = 0; i < parameters.length; i++) {
                    String k = MString.beforeIndex(parameters[i], '=');
                    String v = MString.afterIndex(parameters[i], '=');
                    if (api.setParameter(path + "@" + k, v, timeout, !writable, persistent, local))
                        System.out.println(k + " SET");
                    else System.out.println(k + " NOT CHANGED");
                }
            }
        } else if (cmd.equals("remove")) {
            if (api.removeParameter(path)) System.out.println("REMOVED");
            else System.out.println("UNKNOWN");
        } else if (cmd.equals("publish")) {
            api.publishAll();
        } else if (cmd.equals("request")) {
            api.requestAll();
        } else if (cmd.equals("save")) {
            api.save();
            System.out.println("SAVED");
        } else if (cmd.equals("load")) {
            api.load();
            System.out.println("LOADED");
        }
        return null;
    }
}
