/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.impl.aaa;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.core.security.ModifyAuthorizationApi;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.ModifyTrustApi;

@Command(scope = "sop", name = "modifyaccess", description = "Modify Access Actions, you need admin access to use this command")
@Service
public class ModifyAccessCmd implements Action {

	@Argument(index=0, name="entity", required=true, description="user, trust, auth", multiValued=false)
	String entity;

	@Argument(index=1, name="action", required=true, description="create, password, remove, set, show", multiValued=false)
    String action;
	
	@Argument(index=2, name="name", required=true, description="name/id of the entity", multiValued=false)
    String name;
	
	@Argument(index=3, name="parameters", required=false, description=
			"Command:\n"
			+ " user create <name> <password> *[key=value]\n"
			+ " user password <name> <newPassword>\n"
			+ " user set <name> *[key=value]\n"
			+ " user delete <name>\n"
			+ " user add <name> <group>\n"
			+ " user remove <name> <group>\n"
			+ " trust create <name> <password> *[key=value]\n"
			+ " trust password <name> <newPassword>\n"
			+ " trust set <name> *[key=value]\n"
			+ " trust delete <name>\n"
			+ " auth create <name> <acl>\n"
			+ " auth remove <name>\n"
			+ " auth show <name>",
			multiValued=true)
    String[] parameters;
	
	@Override
	public Object execute() throws Exception {

		AccessApi api = MApi.lookup(AccessApi.class);
		if (api == null) {
			System.out.println("SOP API not found");
			return null;
		}
		
		if (entity.equals("user") && action.equals("create")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			MProperties properties = new MProperties();
			for (int i = 1; i < parameters.length; i++)
				properties.setString(MString.beforeIndex(parameters[i], '='), MString.afterIndex(parameters[i], '='));
			modify.createAccount(name, parameters[0], properties);
			System.out.println("OK");
		} else
		if (entity.equals("user") && action.equals("password")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			modify.changePassword(name, parameters[0]);
			System.out.println("OK");
		} else
		if (entity.equals("user") && action.equals("set")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			MProperties properties = new MProperties();
			for (int i = 0; i < parameters.length; i++)
				properties.setString(MString.beforeIndex(parameters[i], '='), MString.afterIndex(parameters[i], '='));
			modify.changeAccount(name, properties);
			System.out.println("OK");
		} else
		if (entity.equals("user") && action.equals("delete")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			modify.deleteAccount(name);
			System.out.println("OK");
		} else
		if (entity.equals("user") && action.equals("add")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			modify.appendGroups(name, parameters[0]);
			System.out.println("OK");
		} else
		if (entity.equals("user") && action.equals("remove")) {
			ModifyAccountApi modify = api.getModifyAccountApi();
			modify.removeGroups(name, parameters[0]);
			System.out.println("OK");
		} else
		if (entity.equals("trust") && action.equals("create")) {
			ModifyTrustApi modify = api.getModifyTrustApi();
			MProperties properties = new MProperties();
			for (int i = 1; i < parameters.length; i++)
				properties.setString(MString.beforeIndex(parameters[i], '='), MString.afterIndex(parameters[i], '='));
			modify.createTrust(name, parameters[0], properties);
			System.out.println("OK");
		} else
		if (entity.equals("trust") && action.equals("password")) {
			ModifyTrustApi modify = api.getModifyTrustApi();
			modify.changePassword(name, parameters[0]);
			System.out.println("OK");
		} else
		if (entity.equals("trust") && action.equals("set")) {
			ModifyTrustApi modify = api.getModifyTrustApi();
			MProperties properties = new MProperties();
			for (int i = 0; i < parameters.length; i++)
				properties.setString(MString.beforeIndex(parameters[i], '='), MString.afterIndex(parameters[i], '='));
			modify.changeTrust(name, properties);
			System.out.println("OK");
		} else
		if (entity.equals("trust") && action.equals("delete")) {
			ModifyTrustApi modify = api.getModifyTrustApi();
			modify.deleteTrust(name);
			System.out.println("OK");
		} else
		if (entity.equals("auth") && action.equals("create")) {
			ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
			modify.createAuthorization(name, parameters[0]);
			System.out.println("OK");
		} else
		if (entity.equals("auth") && action.equals("remove")) {
			ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
			modify.deleteAuthorization(name);
			System.out.println("OK");
		} else
		if (entity.equals("auth") && action.equals("show")) {
			ModifyAuthorizationApi modify = api.getModifyAuthorizationApi();
			String acl = modify.getAuthorizationAcl(name);
			System.out.println(acl);
		} else {
			System.out.println("Entity or Action not found");
		}
			
		
		return null;
	}

}
