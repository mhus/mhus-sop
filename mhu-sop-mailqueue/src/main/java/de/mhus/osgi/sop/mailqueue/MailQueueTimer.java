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

import java.io.File;
import java.util.Date;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.mail.MSendMail;
import de.mhus.lib.core.mail.MailAttachment;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.services.scheduler.SchedulerService;
import de.mhus.osgi.services.scheduler.SchedulerServiceAdapter;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation;

@Component(provide=SchedulerService.class,immediate=true,properties="interval=*/15 * * * * *")
public class MailQueueTimer extends SchedulerServiceAdapter {

	private static final int MAX_ATTEMPTS = 10;

	@Override
	public void run(Object environment) {
		try {
			Date now = new Date();
			XdbService manager = MApi.lookup(SopApi.class).getManager();
			for (SopMailTask task : manager.getByQualification(Db.query(SopMailTask.class).eq(SopMailTask::getStatus, MailQueueOperation.STATUS.READY).le(SopMailTask::getNextSendAttempt, now))) {
				try {
					sendMail(task);
					task.setStatus(MailQueueOperation.STATUS.SENT);
					task.save();
				} catch (Throwable t) {
					log().e(t);
					task.setSendAttempts(task.getSendAttempts()+1);
					if (task.getSendAttempts() > MAX_ATTEMPTS) {
						task.setStatus(MailQueueOperation.STATUS.ERROR);
					} else {
						task.setNextSendAttempt(new Date(System.currentTimeMillis() + MTimeInterval.MINUTE_IN_MILLISECOUNDS * 15));
					}
					task.setLastError(t.toString());
					task.save();
				}
			}
		} catch (Throwable t) {
			log().e(t);
		}
	}

	private void sendMail(SopMailTask task) throws Exception {
		
		MProperties source = MailQueueOperationImpl.getSourceConfig(task);
		
		File dir = MailQueueOperationImpl.getMailFolder(task);

		MProperties prop = MProperties.load(new File(dir,"config.properties"));
		
		MailAttachment[] attachments = null;
		if (prop.getInt("attachmnets", 0) > 0) {
			attachments = new MailAttachment[prop.getInt("attachmnets", 0)];
			for (int i = 0; i < attachments.length; i++) {
				String name = MFile.getFileName(MUri.toUri(prop.getString("attachment" + i)).getPath());
				attachments[i] = new MailAttachment(new File(dir,"attachment" + i), name, false);
			}
		}
		
		String html = MFile.readFile(new File(dir,"content.html"));
		log().d("send",task);
		String to = source.getString("to", task.getTo() );
		String cc = source.getString("cc", null);
		String bcc = source.getString("bcc", null);
		
		MApi.lookup(MSendMail.class).sendHtmlMail(task.getFrom(), new String[] {to}, new String[] {cc}, new String[] {bcc}, task.getSubject(), html, attachments);
		
		if (source.getBoolean("cleanupAfterSent", true)) {
			log().d("cleanup",task,dir);
			MFile.deleteDir(dir);
		}
		
	}
	
}
