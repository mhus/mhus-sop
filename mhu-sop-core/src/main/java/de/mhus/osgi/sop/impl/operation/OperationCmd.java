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
package de.mhus.osgi.sop.impl.operation;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.form.ModelUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.impl.util.PingOperation;

@Command(scope = "sop", name = "operation", description = "Operation commands")
@Service
public class OperationCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + " list\n"
                            + " action\n"
                            + " info <path>\n"
                            + " execute <path> [key=value]*\n"
                            + " search\n"
                            + " ping [ident]",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "path",
            required = false,
            description = "Path to Operation",
            multiValued = false)
    String path;

    @Argument(
            index = 2,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    //	@Option(name="-c", aliases="--connection", description="JMS Connection Name",required=false)
    //	String conName = null;

    //	@Option(name="-q", aliases="--queue", description="JMS Connection Queue
    // OperationChannel",required=false)
    //	String queueName = null;

    @Option(
            name = "-v",
            aliases = "--version",
            description = "Version Range [1.2.3,2.0.0)",
            required = false)
    String version = null;

    @Option(
            name = "-o",
            aliases = "--options",
            description = "Execute Options separated by pipe",
            required = false)
    String options = null;

    @Option(name = "-p", aliases = "--print", description = "Print File Content", required = false)
    boolean print = false;

    @Override
    public Object execute2() throws Exception {

        //		if (conName == null)
        //			conName = M.l(JmsApi.class).getDefaultConnectionName();

        OperationApi api = M.l(OperationApi.class);

        if (cmd.equals("ping")) {
            LinkedList<String> tags = new LinkedList<>();
            if (parameters != null) tags.add(OperationDescriptor.TAG_IDENT + "=" + parameters[0]);
            OperationDescriptor desc =
                    api.findOperation(PingOperation.class.getCanonicalName(), null, tags);
            long now = System.currentTimeMillis();
            OperationResult res = api.doExecute(desc, new MProperties());
            if (!res.isSuccessful()) throw new MException("Ping not successful");
            MProperties map = res.getResultAs();
            long other = map.getLong("time", 0);
            if (other <= 0) throw new MException("No time sent");
            long diff = other - now;
            System.out.println("Time difference: " + diff);
        } else if (cmd.equals("list")) {
            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("address", "title", "tags", "acl", "parameters", "uuid");
            for (OperationDescriptor desc :
                    api.findOperations(
                            path, version == null ? null : new VersionRange(version), null)) {
                out.addRowValues(
                        desc.getAddress(),
                        desc.getTitle(),
                        desc.getTags(),
                        desc.getAcl(),
                        OperationUtil.getParameters(desc),
                        desc.getUuid());
            }
            out.print(System.out);
        } else if (cmd.equals("info")) {
            OperationDescriptor desc = null;
            if (path.indexOf("://") >= 0) {
                OperationAddress addr = new OperationAddress(path);
                desc = api.getOperation(addr);
            } else
                desc =
                        api.findOperation(
                                path, version == null ? null : new VersionRange(version), null);
            System.out.println("Title  : " + desc.getTitle());
            String xml = null;
            try {
                xml = MXml.toString(ModelUtil.toXml(desc.getForm()), true);
            } catch (Throwable t) {
            }
            System.out.println("Form   : " + xml);
        } else if (cmd.equals("execute")) {
            String[] executeOptions = null;
            if (options != null) executeOptions = options.split("\\|");

            MProperties properties = MProperties.explodeToMProperties(parameters);
            OperationResult res = null;
            if (path.indexOf("://") >= 0) {
                OperationAddress addr = new OperationAddress(path);
                OperationDescriptor desc = api.getOperation(addr);
                res = api.doExecute(desc, properties, executeOptions);
            } else
                res =
                        api.doExecute(
                                path,
                                version == null ? null : new VersionRange(version),
                                null,
                                properties,
                                executeOptions);
            System.out.println("Result: " + res);
            if (res != null) {
                System.out.println("MSG: " + res.getMsg());
                System.out.println("RC : " + res.getReturnCode());
                System.out.println("RES: " + res.getResult());
                if (print && res.getResult() instanceof File) {
                    System.out.println("--- Start File Content ---");
                    FileInputStream is = new FileInputStream((File) res.getResult());
                    MFile.copyFile(is, System.out);
                    System.out.println("\n--- End File Content ---");
                    is.close();
                }
            }
        } else if (cmd.equals("request")) {
            M.l(JmsApi.class).requestOperationRegistry();
            System.out.println("ok");
        } else if (cmd.equals("send")) {
            M.l(JmsApi.class).sendLocalOperations();
            System.out.println("ok");
        } else if (cmd.equals("sync")) {
            api.synchronize();
            System.out.println("ok");
        } else if (cmd.equals("providers")) {
            for (String p : api.getProviderNames()) {
                System.out.println(p);
            }
        } else if (cmd.equals("reset")) {
            api.reset();
            System.out.println("OK");
        } else {
            System.out.println("Command not found");
        }
        return null;
    }
}
