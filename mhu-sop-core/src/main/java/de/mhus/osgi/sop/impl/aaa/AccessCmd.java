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

import java.io.File;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.core.security.ModifyCurrentAccountApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.util.SopUtil;
import de.mhus.osgi.sop.impl.AaaContextImpl;
import de.mhus.osgi.sop.impl.aaa.util.AccountFile;

@Command(scope = "sop", name = "access", description = "Access actions")
@Service
public class AccessCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command:\n"
			+ " login <account>  - login\n"
			+ " logout           - logout\n"
			+ " admin            - login as admin\n"
			+ " reset            - reset user context\n"
			+ " id\n"
			+ " reload           - reload current user context\n"
			+ " info             - info about the current user\n"
			+ " info user <account>\n"
			+ " info trust <trust name>\n"
			+ " info auth <name>\n"
			+ " synchronize <account>\n"
			+ " validate <account> <password>\n"
			+ " synchronizer <type>\n"
			+ " access <account> <name> [<action>]\n"
			+ " reloadconfig\n"
			+ " md5 <password>\n"
			+ " idtree\n"
			+ " passwd [<password>] - set password to current account\n"
			+ " modify [key=value]* - set parameters to current account",
			multiValued=false)
	String cmd;
	
	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;
	
	@Option(name="-a", aliases="--admin", description="Connect user as admin",required=false)
	boolean admin = false;

	@Override
	public Object execute() throws Exception {

		AccessApi api = M.l(AccessApi.class);
		if (api == null) {
			System.out.println("SOP API not found");
			return null;
		}
		
		if (cmd.equals("validate")) {
			Account account = api.getAccount(parameters[0]);
			System.out.println("Result: " + api.validatePassword(account, parameters[1] ) );
		} else
		if (cmd.equals("login")) {
			Account ac = api.getAccount(parameters[0]);
			AaaContext cur = api.process(ac, null, admin,  parameters.length > 1 ? Locale.forLanguageTag(parameters[1]) : null);
			System.out.println(cur);
		} else
		if (cmd.equals("admin")) {
			RootContext context = new RootContext();
			context.setAdminMode(true);
			api.process(context);
			System.out.println(context);
		} else
		if (cmd.equals("logout")) {
			AaaContext cur = api.getCurrentOrGuest();
			cur = api.release(cur.getAccount());
			System.out.println(cur);
		} else
		if (cmd.equals("reset")) {
			api.resetContext();
		} else
		if (cmd.equals("id")) {
			AaaContext cur = api.getCurrentOrGuest();
			System.out.println(cur);
			if (cur.isAdminMode())
				System.out.println("Admin mode");
		} else
		if (cmd.equals("reload")) {
			Account ac = api.getCurrentAccount();
			System.out.println("Reload: " + ac.reloadAccount());
		} else
		if (cmd.equals("idtree")) {
			AaaContextImpl cur = (AaaContextImpl) api.getCurrentOrGuest();
			while (cur != null) {
				System.out.println(cur);
				cur = cur.getParent();
			}
			System.out.println(cur);
		} else
		if (cmd.equals("root")) {
			api.resetContext();
			AaaContext cur = api.processAdminSession();
			System.out.println(cur);
		} else
		if (cmd.equals("group")) {
			Account ac = api.getAccount(parameters[0]);
			return ac.hasGroup(parameters[1]);
		} else
		if (cmd.equals("access")) {
			Account ac = api.getAccount(parameters[0]);
			if (parameters.length > 2)
				return api.hasGroupAccess(ac, parameters[1], parameters[2], null);
			else
				return api.hasGroupAccess(ac, parameters[1], null, null);
		} else
		if (cmd.equals("info")) {
			if (parameters == null || parameters.length == 0) {
				Account ac = api.getCurrentAccount();
				System.out.println(ac);
				for (Entry<String, Object> attr : ac.getAttributes().entrySet()) {
					System.out.println(attr.getKey() + "=" + attr.getValue());
				}
				try {
					ModifyAccountApi modify = api.getModifyAccountApi();
					for (String group : modify.getGroups(parameters[1])) {
						System.out.println("Group: " + group);
					}
				} catch (Throwable t) {}
			} else
			if (parameters[0].equals("user")) {
				Account ac = api.getAccount(parameters[1]);
				System.out.println(ac);
				for (Entry<String, Object> attr : ac.getAttributes().entrySet()) {
					System.out.println(attr.getKey() + "=" + attr.getValue());
				}
				try {
					ModifyAccountApi modify = api.getModifyAccountApi();
					for (String group : modify.getGroups(parameters[1])) {
						System.out.println("Group: " + group);
					}
				} catch (Throwable t) {}
			} else
			if (parameters[0].equals("trust")) {
				try {
					Trust ac = api.getModifyTrustApi().getTrust(parameters[1]);
					System.out.println(ac);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else
			if (parameters[0].equals("auth")) {
				try {
					String ac = api.getModifyAuthorizationApi().getAuthorizationAcl(parameters[1]);
					System.out.println(ac);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else
				System.out.println("Type not found");
				
		} else
		if(cmd.equals("controllers")) {
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("Type","Controller","Bundle");
			AdbApi adb = M.l(AdbApi.class);
			for (Entry<String, DbSchemaService> entry : adb.getController()) {
				Bundle bundle = FrameworkUtil.getBundle(entry.getValue().getClass());
				table.addRowValues(entry.getKey(), entry.getValue().getClass(), bundle.getSymbolicName() );
			}
			table.print(System.out);
		} else
		if (cmd.equals("md5")) {
			System.out.println( MPassword.encodePasswordMD5(parameters[0]) );
		} else
		if (cmd.equals("passwd")) {
			Account current = api.getCurrentAccount();
			ModifyCurrentAccountApi modify = api.getModifyCurrentAccountApi();
			if (modify == null) {
				System.out.println("Modify is not supported");
				return null;
			}
			System.out.println("Change password for " + current.getName());
			String newPw = null;
			if (parameters != null && parameters.length > 0 && parameters[0] != null) {
				newPw = parameters[0];
			} else {
				System.out.print("New Password: ");
				System.out.flush();
				newPw = Console.get().readPassword();
				System.out.print("Again Password: ");
				System.out.flush();
				if (!newPw.equals(Console.get().readPassword())) {
					System.out.println("Passwords do not equal");
					return null;
				}
			}
			modify.changePassword(newPw);
			current.reloadAccount();
			System.out.println("Changed");
		} else
		if (cmd.equals("modify")) {
			Account current = api.getCurrentAccount();
			ModifyCurrentAccountApi modify = api.getModifyCurrentAccountApi();
			if (modify == null) {
				System.out.println("Modify is not supported");
				return null;
			}
			System.out.println("Modify account for " + current.getName());
			MProperties p = MProperties.explodeToMProperties(parameters);
			p.putReadProperties(current.getAttributes());
			modify.changeAccount(p);
			System.out.println("Changed, Current " + current.reloadAccount());
		} else
		if (cmd.equals("migrateFilesToCurrent")) {
			String path = "aaa/account/";
			File dir = SopUtil.getFile( path );
			for (File file : dir.listFiles()) {
				System.out.println(">>> Migrate " + file.getName());
				try {
					if (file.isFile() && file.getName().endsWith(".xml")) {
						AccountFile from = new AccountFile(file,MFile.getFileNameOnly(file.getName()));
						ModifyAccountApi mod = api.getModifyAccountApi();
						mod.createAccount(from.getName(), UUID.randomUUID().toString(), from.getAttributes());
						mod.appendGroups(from.getName(), from.getGroups());
						mod.changePasswordInternal(from.getName(), from.getMd5Password());
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		} else
			System.out.println("Command not found: " + cmd);
			
		
		return null;
	}

}
