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
package de.mhus.osgi.sop.mailqueue;

import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation.STATUS;
import de.mhus.osgi.sop.api.operation.OperationUtil;

@Command(scope = "sop", name = "mailqueue", description = "Main queue actions")
@Service
public class MailQueueCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command:\n"
			+ " new <source> <from> <to> <subject> <content html> [attachments]\n"
			+ " list\n"
			+ " status <id>\n"
			+ " retry [<id>]\n"
			+ " lost [<id>]\n"
			+ " clenanup\n"
			+ " delete <id>")
	String cmd;

	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-a", aliases="--all", description="All",required=false)
	boolean all = false;
	
	@Option(name="-f", aliases="--force", description="Force action",required=false)
	boolean force = false;
	
	@Option(name="-ct", aliases="--table", description="Table output",required=false)
	String ct;
	
	@Override
	public Object execute() throws Exception {

		switch (cmd) {
		case "list": {
			
			ConsoleTable table = new ConsoleTable(ct);
			table.setHeaderValues("id","source","status","next","to","subject","attempts", "created");
			
			XdbService manager = MApi.lookup(SopApi.class).getManager();
			AQuery<SopMailTask> q = Db.query(SopMailTask.class);
			if (!all)
				q.eq(SopMailTask::getStatus, MailQueueOperation.STATUS.READY);
			for (SopMailTask task : manager.getByQualification(q)) {
				table.addRowValues(
						task.getId(),
						task.getSource(),
						task.getStatus(),
						task.getNextSendAttempt(),
						task.getTo(),
						task.getSubject(),
						task.getSendAttempts(),
						task.getCreationDate()
						);
			}
			
			table.print(System.out);
		} break;
		case "new": {
			MailQueueOperation mq = OperationUtil.getOperationIfc(MailQueueOperation.class);
			String[] attachments = null;
			if (parameters.length > 4) {
				attachments = new String[parameters.length-4];
				for (int i = 5; i < parameters.length; i++)
					attachments[i-5] = parameters[i];
			}
			UUID id = mq.scheduleHtmlMail(parameters[0], parameters[1], parameters[2].split(";"), parameters[3], parameters[4], attachments);
			System.out.println("Scheduled as " + id);
		} break;
		case "status":{
			MailQueueOperation mq = OperationUtil.getOperationIfc(MailQueueOperation.class);
			UUID id = UUID.fromString(parameters[0]);
			STATUS status = mq.getStatus(id);
			System.out.println("Status: " + status);
		} break;
		case "retry": {
			if (parameters == null || parameters.length == 0) {
				// retry all
				XdbService manager = MApi.lookup(SopApi.class).getManager();
				for (SopMailTask task : manager.getByQualification(Db.query(SopMailTask.class).eq(SopMailTask::getStatus, MailQueueOperation.STATUS.ERROR))) {
					System.out.println(task);
					task.setStatus(STATUS.READY);
					task.save();
				}
			} else {
				UUID id = UUID.fromString(parameters[0]);
				SopApi api = MApi.lookup(SopApi.class);
				SopMailTask task = api.getManager().getObject(SopMailTask.class, id);
				if (force || task.getStatus() == STATUS.ERROR || task.getStatus() == STATUS.ERROR_PREPARE) {
					task.setStatus(STATUS.READY);
					task.save();
					System.out.println("OK");
				} else {
					System.out.println("Task is not in ERROR");
				}
			}
		} break;
		case "lost": {
			if (parameters == null || parameters.length == 0) {
				// retry all
				XdbService manager = MApi.lookup(SopApi.class).getManager();
				for (SopMailTask task : manager.getByQualification(Db.query(SopMailTask.class).eq(SopMailTask::getStatus, MailQueueOperation.STATUS.ERROR))) {
					System.out.println(task);
					task.setStatus(STATUS.LOST);
					task.save();
				}
			} else {
				UUID id = UUID.fromString(parameters[0]);
				SopApi api = MApi.lookup(SopApi.class);
				SopMailTask task = api.getManager().getObject(SopMailTask.class, id);
				if (force || task.getStatus() == STATUS.ERROR || task.getStatus() == STATUS.ERROR_PREPARE) {
					task.setStatus(STATUS.LOST);
					task.save();
					System.out.println("OK");
				} else {
					System.out.println("Task is not in ERROR");
				}
			}
		} break;
		case "cleanup": {
			XdbService manager = MApi.lookup(SopApi.class).getManager();
			for (SopMailTask task : manager.getByQualification(Db.query(SopMailTask.class).eq(SopMailTask::getStatus, MailQueueOperation.STATUS.ERROR))) {
				if (task.getStatus() == STATUS.NEW || task.getStatus() == STATUS.READY || task.getStatus() == STATUS.ERROR) {
					// ignore
				} else {
					System.out.println(task);
					task.delete();
				}
			}
		} break;
		case "delete": {
			UUID id = UUID.fromString(parameters[0]);
			SopApi api = MApi.lookup(SopApi.class);
			SopMailTask task = api.getManager().getObject(SopMailTask.class, id);
			if (force || task.getStatus() == STATUS.ERROR || task.getStatus() == STATUS.ERROR_PREPARE || task.getStatus() == STATUS.SENT || task.getStatus() == STATUS.LOST) {
				task.setStatus(STATUS.LOST);
				task.delete();
				System.out.println("Deleted");
			} else {
				System.out.println("Task is not in ERROR");
			}
			
		} break;
		}
		
		return null;
	}
}
