package de.mhus.osgi.sop.mailqueue;

import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation.STATUS;
import de.mhus.osgi.sop.api.operation.OperationUtil;

@Command(scope = "sop", name = "mailqueue", description = "Main queue actions")
@Service
public class MailQueueCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command:\n"
			+ " send <source> <from> <to> <subject> <content html> [attachments]")
	String cmd;

	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Override
	public Object execute() throws Exception {

		switch (cmd) {
		case "send": {
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
			UUID id = UUID.fromString(parameters[0]);
			SopApi api = MApi.lookup(SopApi.class);
			SopMailTask task = api.getManager().getObject(SopMailTask.class, id);
			if (task.getStatus() == STATUS.ERROR) {
				task.setStatus(STATUS.READY);
				task.save();
				System.out.println("OK");
			}
		} break;
		}
		
		return null;
	}
}
