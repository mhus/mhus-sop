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
package de.mhus.osgi.sop.foundation.data;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.data.SopDataController;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.model.SopData;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;

@Command(scope = "sop", name = "data", description = "Sop Data actions")
@Service
public class SopDataCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command: "
			+ " list <foundId> [<type>] [<search>]\n"
			+ " set <id> [<data>]* (data:=)\n"
			+ " get <id>\n"
			+ " create <found> <type> [<data>]* (data:=_value1..5=value|_foreignid=value|public|rw|ro|key=value)\n"
			+ " touch [found|id] [found:<type>]\n"
			+ " delete <id>\n"
			+ " ro/rw/public/private/sync/archive/unarchive <id>\n"
			+ " syncListBeforeLoad <found> <type> [<search>] [<archived>] [<due>]")
	String cmd;
	
	@Argument(index=1, name="parameters", required=false, description="Parameters", multiValued=true)
	String[] params;

	@Option(name="-r", aliases="--r", description="Use RAW Database commands",required=false)
	boolean raw = false;
	
	@Option(name="-a", aliases="--all", description="For mass updates/list, update/list also archived objects",required=false)
	boolean all = false;
	
	@Option(name="-s", aliases="--sync", description="Use build in sync mechanism while loading dataobject",required=false)
	boolean sync = false;
	
	@Override
	public Object execute() throws Exception {
		
		FoundationApi api = MApi.lookup(FoundationApi.class);
		
		if (cmd.equals("list")) {
			SopFoundation found = api.getFoundation(params[0]);
			
			ConsoleTable out = new ConsoleTable();
			out.setHeaderValues("id","type","public","writable","foreignid","archived","value1","value2","value3","value4","value5","ForeignDate");
			List<SopData> res = api.getSopData(found.getId(), params.length > 1 ? params[1] : null, params.length > 2 ? params[2] : null, false, all ? null : false, null);
			for (SopData d : res) {
				out.addRowValues(d.getId(),d.getType(),d.isIsPublic(),d.isIsWritable(),d.getForeignId(),d.isArchived(),d.getValue1(),d.getValue2(),d.getValue3(),d.getValue4(),d.getValue5(),d.getForeignDate());
			}
			out.print(System.out);
		} else
		if (cmd.equals("get")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (!raw) {
				SopDataController xcontroller = api.getDataSyncControllerForType(d.getType());
				xcontroller.doPrepareForOutput(d, null, false);
			}
			System.out.println("Id          : " + d.getId());
			System.out.println("Type        : " + d.getType());
			System.out.println("Status      : " + d.getStatus());
			System.out.println("Due         : " + d.getDue());
			System.out.println("Public      : " + d.isIsPublic());
			System.out.println("Writable    : " + d.isIsWritable());
			System.out.println("Archived    : " + d.isArchived());
			System.out.println("ForeignId   : " + d.getForeignId());
			System.out.println("ForeignDate : " + d.getForeignDate());
			System.out.println("Value1      : " + d.getValue1());
			System.out.println("Value2      : " + d.getValue2());
			System.out.println("Value3      : " + d.getValue3());
			System.out.println("Value4      : " + d.getValue4());
			System.out.println("Value5      : " + d.getValue5());
			System.out.println("--- Technical");
			System.out.println("LastSync    : " + d.getLastSync());
			System.out.println("LastSyncTry : " + d.getLastSyncTry());
			System.out.println("LastSyncMsg : " + d.getLastSyncMsg());
			System.out.println("Vstamp      : " + d.getVstamp());
			System.out.println("Created     : " + d.getCreationDate());
			System.out.println("Modified    : " + d.getModifyDate());
			System.out.println("Foundation  : " + d.getFoundation());
			System.out.println("--- Data");
			for (Entry<String, Object> item :  new TreeMap<String, Object>(d.getData()).entrySet()) {
				System.out.println(item.getKey() + "=" + item.getValue());
			}

		} else
		if (cmd.equals("set")) {
			SopData d = api.getSopData(null, params[0], sync);
			for (int i=1; i < params.length; i++) {
				String pair = params[i];
				String key = MString.beforeIndex(pair, '=');
				String val = MString.afterIndex(pair, '=');
				
				if (key.equals("_foreignid")) {
					d.setForeignId(val);
				} else
				if (key.equals("_status")) {
					d.setStatus(val);
				} else
				if (key.equals("_due")) {
					d.setDue(MDate.toDate(val, null));
				} else
				if (key.equals("_value1")) {
					d.setValue1(val);
				} else
				if (key.equals("_value2")) {
					d.setValue2(val);
				} else
				if (key.equals("_value3")) {
					d.setValue3(val);
				} else
				if (key.equals("_value4")) {
					d.setValue4(val);
				} else
				if (key.equals("_value5")) {
					d.setValue5(val);
				} else {
					d.getData().setString(key, val);
				}
			}
			if (raw)
				d.save();
			else {
				SopDataController xcontroller = api.getDataSyncControllerForType(d.getType());
				xcontroller.updateSopData(d);
			}
			System.out.println("SAVED " + d);
		} else
		if (cmd.equals("create")) {
			
			SopFoundation found = api.getFoundation(params[0]);
			if (found == null) {
				System.out.println("foundnization not found");
				return null;
			}
			
			SopDataController xcontroller = api.getDataSyncControllerForType(params[1]);
			if (xcontroller == null) {
				System.out.println("Type not found");
				return null;
			}
			
			SopData d = MApi.lookup(SopApi.class).getManager().inject(new SopData(found, params[1]));
			
			
			for (int i=2; i < params.length; i++) {
				String pair = params[i];
				
				if (pair.equals("public")) {
					d.setPublic(true);
				} else
				if (pair.equals("rw")) {
					d.setWritable(true);
				} else
				if (pair.equals("private")) {
					d.setPublic(false);
				} else
				if (pair.equals("ro")) {
					d.setWritable(false);
				} else {
					
					String key = MString.beforeIndex(pair, '=');
					String val = MString.afterIndex(pair, '=');
					
					if (key.equals("_foreignid")) {
						d.setForeignId(val);
					} else
					if (key.equals("_status")) {
						d.setStatus(val);
					} else
					if (key.equals("_due")) {
						d.setDue(MDate.toDate(val, null));
					} else
					if (key.equals("_value1")) {
						d.setValue1(val);
					} else
					if (key.equals("_value2")) {
						d.setValue2(val);
					} else
					if (key.equals("_value3")) {
						d.setValue3(val);
					} else
					if (key.equals("_value4")) {
						d.setValue4(val);
					} else
					if (key.equals("_value5")) {
						d.setValue5(val);
					} else
					if (key.equals("_writable")) {
						d.setWritable(MCast.toboolean(val, false));
					} else
					if (key.equals("_public")) {
						d.setPublic(MCast.toboolean(val, false));
					} else {
						d.getData().setString(key, val);
					}
				}
			}
			if (raw)
				d.save();
			else
				xcontroller.createSopData(d);
			
			System.out.println("CREATED " + d);

		} else
		if (cmd.equals("delete")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (raw)
				d.delete();
			else {
				SopDataController xcontroller = api.getDataSyncControllerForType(d.getType());
				xcontroller.deleteSopData(d);
			}
				
			System.out.println("DELETED " + d);
		} else
		if (cmd.equals("archive")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (!d.isArchived()) {
				d.setArchived(true);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("unarchive")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (d.isArchived()) {
				d.setArchived(false);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("rw")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (!d.isIsWritable()) {
				d.setWritable(true);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("ro")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (d.isIsWritable()) {
				d.setWritable(false);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("public")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (!d.isIsPublic()) {
				d.setPublic(true);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("private")) {
			SopData d = api.getSopData(null, params[0], sync);
			if (d.isIsPublic()) {
				d.setPublic(false);
				d.save();
				System.out.println("UPDATE " + d);
			} else {
				System.out.println("SKIP " + d);
			}
		} else
		if (cmd.equals("touch")) {
			if (params.length == 0) {
			} else
			if (MValidator.isUUID(params[0])) {
				SopData d = api.getSopData(null, params[0], sync);
				SopDataController xcontroller = api.getDataSyncControllerForType(d.getType());
				if (xcontroller != null) {
					xcontroller.updateSopData(d);
					System.out.println("UPDATE " + d);
				} else
					System.out.println("xcontroller not found: " + d);
			} else {
				SopFoundation found = api.getFoundation(params[0]);
				System.out.println("foundnization: " + found);
				List<SopData> res = api.getSopData(found.getId(), params.length > 1 ? params[1] : null, params.length > 2 ? params[2] : null, false, all ? null : false, null);
				for (SopData d : res) {
					SopDataController xcontroller = api.getDataSyncControllerForType(d.getType());
					if (xcontroller != null) {
						xcontroller.updateSopData(d);
						System.out.println("UPDATE " + d);
					} else
						System.out.println("xcontroller not found: " + d);
				}
			}
			
		} else
		if (cmd.equals("sync")) {
			if (params.length == 0) {
			} else
			if (MValidator.isUUID(params[0])) {
				SopData d = api.getSopData(null, params[0], sync);
				api.syncSopData(d, true, true);
				System.out.println("SYNCED " + d);
			} else {
				SopFoundation found = api.getFoundation(params[0]);
				System.out.println("foundnization: " + found);
				List<SopData> res = api.getSopData(found.getId(), params.length > 1 ? params[1] : null, params.length > 2 ? params[2] : null, false, all ? null : false, null);
				for (SopData d : res) {
					api.syncSopData(d, true, true);
					System.out.println("SYNCED " + d);
				}
			}
		} else
		if (cmd.equals("syncListBeforeLoad")) {
//			DataObject d = hfo.getDataObject(params[0]);
			
			SopFoundation found = api.getFoundation(params[0]);
			String type = params[1];
			String search = params.length > 2 ? params[2] : null;
			Boolean archived = params.length > 3 && !params[3].equals("?") ? MCast.toboolean(params[3], false) : null;
			Date due = params.length > 4 && !params[4].equals("?") ? MCast.toDate(params[4], null) : null;
			
			SopDataController xcontroller = api.getDataSyncControllerForType(type);
			
			List<SopData> list = api.getSopData(found.getId(), type, search, true, archived, due);
			
			xcontroller.syncListBeforeLoad(found, type, search, archived, due, list);
		}
		return null;
	}


}
