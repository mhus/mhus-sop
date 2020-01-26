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
package de.mhus.osgi.sop.impl.adb;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.model.SopAcl;

@Command(scope = "sop", name = "acl", description = "Handle acl objects")
@Service
public class AclCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command list <id>, get <id>, create <id> <acl>, set <id> <acl>, delete <id>",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        SopApi api = M.l(SopApi.class);

        switch (cmd) {
            case "list":
                {
                    ConsoleTable out = new ConsoleTable(tblOpt);
                    out.setHeaderValues("target", "acl", "id");
                    for (SopAcl acl :
                            SopDbImpl.getManager()
                                    .getByQualification(
                                            Db.query(SopAcl.class)
                                                    .like("target", parameters[0] + "%"))) {
                        out.addRowValues(acl.getTarget(), acl.getList(), acl.getId());
                    }
                    out.print(System.out);
                }
                break;
            case "get":
                {
                    SopAcl acl = api.getAcl(parameters[0]);
                    if (acl == null) System.out.println("ACL not found");
                    else {
                        System.out.println("Id      : " + acl.getId());
                        System.out.println("Created : " + acl.getCreationDate());
                        System.out.println("Modified: " + acl.getModifyDate());
                        System.out.println("Target  : " + acl.getTarget());
                        System.out.println("ACL     : " + acl.getList());
                    }
                }
                break;
            case "create":
                {
                    try (AaaContext ctx = AaaUtil.enterRoot()) {
                        SopAcl acl =
                                SopDbImpl.getManager()
                                        .inject(new SopAcl(parameters[0], parameters[1]));
                        acl.save();
                        System.out.println("Id     : " + acl.getId());
                        System.out.println("Created: " + acl.getCreationDate());
                        System.out.println("Target : " + acl.getTarget());
                        System.out.println("ACL    : " + acl.getList());
                    }
                }
                break;
            case "set":
                {
                    try (AaaContext ctx = AaaUtil.enterRoot()) {
                        SopAcl acl = api.getAcl(parameters[0]);
                        if (acl == null) System.out.println("ACL not found");
                        else {
                            acl.setList(parameters[1]);
                            acl.save();
                            System.out.println("Id      : " + acl.getId());
                            System.out.println("Created : " + acl.getCreationDate());
                            System.out.println("Modified: " + acl.getModifyDate());
                            System.out.println("Target  : " + acl.getTarget());
                            System.out.println("ACL     : " + acl.getList());
                        }
                    }
                }
                break;
            case "delete":
                {
                    AaaUtil.enterRoot();
                    try (AaaContext ctx = AaaUtil.enterRoot()) {
                        SopAcl acl = api.getAcl(parameters[0]);
                        if (acl == null) System.out.println("ACL not found");
                        else {
                            acl.delete();
                            System.out.println("Deleted");
                        }
                    }
                }
                break;
            default:
                System.out.println("Unknown command");
        }
        return null;
    }
}
