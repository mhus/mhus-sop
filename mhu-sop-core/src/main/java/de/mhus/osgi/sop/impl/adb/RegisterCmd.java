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

package de.mhus.osgi.sop.impl.adb;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopRegister;
import de.mhus.osgi.sop.api.util.RegisterUtil;

@Command(scope = "sop", name = "register", description = "Register actions")
@Service
public class RegisterCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command:\n"
			+ " list <name> [<key1>] [<key2>],\n"
			+ " delete <name> <key1> <key2>,\n"
			+ " set <name> <key1> <key2> <value1> <value2>", multiValued=false)
	String cmd;
	
	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;
	
	@Override
	public Object execute() throws Exception {

		XdbService db = MApi.lookup(SopApi.class).getManager();
		
		if (cmd.equals("list")) {
			
			ConsoleTable table = new ConsoleTable();
		
			table.setHeaderValues("Key1","Key2","Value1","Value2");
			AQuery<SopRegister> query = Db.query(SopRegister.class).eq("name", parameters[0]);
			if (parameters.length > 1) query.eq("key1",parameters[1]);
			if (parameters.length > 2) query.eq("key2",parameters[2]);
			
			for (SopRegister r : db.getByQualification(query))
				table.addRowValues(r.getKey1(), r.getKey2(), r.getValue1(), r.getValue2());
		
			table.print(System.out);
			
		} else
		if (cmd.equals("delete")) {
			
			AQuery<SopRegister> query = Db.query(SopRegister.class).eq("name", parameters[0]);
			if (parameters.length > 1) query.eq("key1",parameters[1]);
			if (parameters.length > 2) query.eq("key2",parameters[2]);
			
			for (SopRegister r : db.getByQualification(query)) {
				System.out.println("DELETE " + r.getKey1() + "," + r.getKey2());
				r.delete();
			}
		} else
		if (cmd.equals("set")) {
			
			SopRegister r = RegisterUtil.set(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4]);
			System.out.println("SAVED " + r.getKey1() + "," + r.getKey2());
			
		} else
			System.out.println("Command not found");

		return null;
	}

}
