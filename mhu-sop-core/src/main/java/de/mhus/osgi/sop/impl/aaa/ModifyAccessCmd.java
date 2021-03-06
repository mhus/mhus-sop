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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.core.security.ModifyAuthorizationApi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.ModifyTrustApi;

@Command(
        scope = "sop",
        name = "modifyaccess",
        description = "Modify Access Actions, you need admin access to use this command")
@Service
public class ModifyAccessCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "entity",
            required = true,
            description = "user, trust, auth",
            multiValued = false)
    String entity;

    @Argument(
            index = 1,
            name = "action",
            required = true,
            description = "create, password, remove, set, show",
            multiValued = false)
    String action;

    @Argument(
            index = 2,
            name = "name",
            required = true,
            description = "name/id of the entity",
            multiValued = false)
    String name;

    @Argument(
            index = 3,
            name = "parameters",
            required = false,
            description =
                    "Command:\n"
                            + " user create <name> <password> *[key=value]\n"
                            + " user password <name> <newPassword>\n"
                            + " user set <name> *[key=value] - will remove all other keys\n"
                            + " user add <name> *[key=value]\n"
                            + " user delete <name>\n"
                            + " user list <filter>\n"
                            + " user active <name> <active> - activate or deactivate user\n"
                            + " group add <name> <group>\n"
                            + " group remove <name> <group>\n"
                            + " trust create <name> <password> *[key=value]\n"
                            + " trust password <name> <newPassword>\n"
                            + " trust set <name> *[key=value]\n"
                            + " trust delete <name>\n"
                            + " auth create <name> <acl>\n"
                            + " auth remove <name>\n"
                            + " auth show <name>",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        AccessApi api = M.l(AccessApi.class);
        if (api == null) {
            System.out.println("SOP API not found");
            return null;
        }

        if (entity.equals("user") && action.equals("create")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            MProperties properties = new MProperties();
            for (int i = 1; i < parameters.length; i++)
                properties.setString(
                        MString.beforeIndex(parameters[i], '='),
                        MString.afterIndex(parameters[i], '='));
            modify.createAccount(name, parameters[0], properties);
            System.out.println("OK");
        } else if (entity.equals("user") && action.equals("password")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            modify.changePassword(name, parameters[0]);
            System.out.println("OK");
        } else if (entity.equals("user") && action.equals("set")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            MProperties properties = new MProperties();
            for (int i = 0; i < parameters.length; i++)
                properties.setString(
                        MString.beforeIndex(parameters[i], '='),
                        MString.afterIndex(parameters[i], '='));
            modify.changeAccount(name, properties);
            System.out.println("OK");
        } else if (entity.equals("user") && action.equals("add")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            Account acc = api.getAccount(name);
            MProperties properties = new MProperties(acc.getAttributes());
            for (int i = 0; i < parameters.length; i++)
                properties.setString(
                        MString.beforeIndex(parameters[i], '='),
                        MString.afterIndex(parameters[i], '='));
            modify.changeAccount(name, properties);
            System.out.println("OK");
        } else if (entity.equals("user") && action.equals("delete")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            modify.deleteAccount(name);
            System.out.println("OK");
        } else if (entity.equals("group") && action.equals("add")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            modify.appendGroups(name, parameters[0]);
            System.out.println("OK");
        } else if (entity.equals("group") && action.equals("remove")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            modify.removeGroups(name, parameters[0]);
            System.out.println("OK");
        } else if (entity.equals("user") && action.equals("list")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            ConsoleTable table = new ConsoleTable(false);
            table.setHeaderValues("Name", "Display Name", "Ident", "Created", "Modified");
            for (String n : modify.getAccountList(name)) {
                Account acc = api.getAccount(n);
                table.addRowValues(
                        n,
                        acc.getDisplayName(),
                        acc.getUUID(),
                        acc.getCreationDate(),
                        acc.getModifyDate());
            }
            table.print();
        } else if (entity.equals("user") && action.equals("active")) {
            ModifyAccountApi modify = api.getModifyAccountApi();
            boolean active = MCast.toboolean(parameters[0], false);
            modify.activateAccount(name, active);
            System.out.println("OK");
        } else if (entity.equals("trust") && action.equals("create")) {
            ModifyTrustApi modify = api.getModifyTrustApi();
            MProperties properties = new MProperties();
            for (int i = 1; i < parameters.length; i++)
                properties.setString(
                        MString.beforeIndex(parameters[i], '='),
                        MString.afterIndex(parameters[i], '='));
            modify.createTrust(name, parameters[0], properties);
            System.out.println("OK");
        } else if (entity.equals("trust") && action.equals("password")) {
            ModifyTrustApi modify = api.getModifyTrustApi();
            modify.changePassword(name, parameters[0]);
            System.out.println("OK");
        } else if (entity.equals("trust") && action.equals("set")) {
            ModifyTrustApi modify = api.getModifyTrustApi();
            MProperties properties = new MProperties();
            for (int i = 0; i < parameters.length; i++)
                properties.setString(
                        MString.beforeIndex(parameters[i], '='),
                        MString.afterIndex(parameters[i], '='));
            modify.changeTrust(name, properties);
            System.out.println("OK");
        } else if (entity.equals("trust") && action.equals("delete")) {
            ModifyTrustApi modify = api.getModifyTrustApi();
            modify.deleteTrust(name);
            System.out.println("OK");
        } else if (entity.equals("auth") && action.equals("create")) {
            ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
            modify.createAuthorization(name, parameters[0]);
            System.out.println("OK");
        } else if (entity.equals("auth") && action.equals("remove")) {
            ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
            modify.deleteAuthorization(name);
            System.out.println("OK");
        } else if (entity.equals("auth") && action.equals("show")) {
            ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
            String acl = modify.getAuthorizationAcl(name);
            System.out.println(acl);
        } else {
            System.out.println("Entity or Action not found");
        }

        return null;
    }
}
