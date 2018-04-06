package de.mhus.osgi.sop.mailqueue;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationToIfcProxy;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.dfs.DfsApi;
import de.mhus.osgi.sop.api.dfs.FileQueueApi;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation;
import de.mhus.osgi.sop.api.util.SopUtil;

@Component(immediate=true,provide=Operation.class)
public class MailQueueOperationImpl extends OperationToIfcProxy implements MailQueueOperation {

	@Override
	public UUID scheduleHtmlMail(String source, String from, String[] to, String subject, String content, String ... attachments) throws MException {
		UUID res = null;
		for (String t : to) {
			UUID r = scheduleHtmlMail(source, from, t, subject, content, attachments);
			if (res == null) res = r;
		}
		
		return res;
	}

	public UUID scheduleHtmlMail(String source, String from, String to, String subject, String content, String ... attachments) throws MException {
		SopApi api = MApi.lookup(SopApi.class);
		// create task
		SopMailTask task = api.getManager().inject(new SopMailTask(source, from, to, subject));
		task.save();
		try {
			// create folder
			File dir = getMailFolder(task);
			
			if (content.startsWith(DfsApi.SCHEME_DFQ + ":")) {
				FileQueueApi dfq = MApi.lookup(FileQueueApi.class);
				File contentFrom = dfq.loadFile(MUri.toUri(content));
				MFile.copyFile(contentFrom, new File(dir,"content.html"));
			} else {
				MFile.writeFile(new File(dir,"content.html"), content);
			}
			MProperties prop = new MProperties();
			
			if (attachments != null && attachments.length > 0) {
				FileQueueApi dfq = MApi.lookup(FileQueueApi.class);
				int cnt = 0;
				for (String atta : attachments) {
					File file = dfq.loadFile(MUri.toUri(atta));
					File dest = new File(dir,"attachment" + cnt);
					MFile.copyFile(file, dest);
					prop.setString("attachment" + cnt, atta);
					cnt++;
				}
				prop.setInt("attachments", cnt);
			}
			
			prop.save(new File(dir,"config.properties"));
			
			// set state of task
			task.setStatus(STATUS.READY);
			task.save();
			return task.getId();
		} catch (Throwable t) {
			log().w(t);
			task.setStatus(STATUS.ERROR_PREPARE);
			task.setLastError(t.toString());
			task.save();
			return null;
		}
	}

	public static File getMailFolder(SopMailTask task) {
		File dir = SopUtil.getFile("mailqueue/mails/" + task.getId());
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	public static MProperties getSourceConfig(SopMailTask task) {
		File file = SopUtil.getFile("mailqueue/sources/" + task.getSource() + ".properties");
		if (!file.exists()) return new MProperties();
		return MProperties.load(file);
	}
	
	@Override
	protected Class<?> getInterfaceClass() {
		return MailQueueOperation.class;
	}

	@Override
	protected Object getInterfaceObject() {
		return this;
	}

	@Override
	protected Version getInterfaceVersion() {
		return MOsgi.getBundelVersion(this.getClass());
	}

	@Override
	protected void initOperationDescription(HashMap<String, String> parameters) {
		
	}

	@Override
	public STATUS getStatus(UUID id) throws MException {
		SopApi api = MApi.lookup(SopApi.class);
		SopMailTask task = api.getManager().getObject(SopMailTask.class, id);
		if (task == null) throw new NotFoundException(id);
		return task.getStatus();
	}

}
