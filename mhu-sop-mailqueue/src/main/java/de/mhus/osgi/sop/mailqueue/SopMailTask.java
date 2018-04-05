package de.mhus.osgi.sop.mailqueue;

import java.util.Date;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.mailqueue.MailQueueOperation;

public class SopMailTask extends DbMetadata {

	@DbPersistent
	private String source;
	@DbPersistent
	private String from;
	@DbPersistent
	private String to;
	@DbPersistent
	private String subject;
	@DbPersistent
	private MailQueueOperation.STATUS status = MailQueueOperation.STATUS.NEW;
	@DbPersistent
	private Date lastSendAttempt;
	@DbPersistent
	private Date nextSendAttempt = new Date();
	@DbPersistent (type=DbType.TYPE.STRING, size=700)
	private String lastError;
	@DbPersistent
	private int sendAttempts = 0;

	public SopMailTask() {}
	
	public SopMailTask(String source, String from, String to, String subject) {
		this.source = source;
		this.from = from;
		this.to = to;
		this.subject = M.trunc(subject, 200);
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	public MailQueueOperation.STATUS getStatus() {
		return status;
	}

	public void setStatus(MailQueueOperation.STATUS status) {
		this.status = status;
	}

	public Date getLastSendAttempt() {
		return lastSendAttempt;
	}

	public void setLastSendAttempt(Date lastSendAttempt) {
		this.lastSendAttempt = lastSendAttempt;
	}

	public Date getNextSendAttempt() {
		return nextSendAttempt;
	}

	public void setNextSendAttempt(Date nextSendAttempt) {
		this.nextSendAttempt = nextSendAttempt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = M.trunc(lastError, 700);
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public int getSendAttempts() {
		return sendAttempts;
	}

	public void setSendAttempts(int sendAttempts) {
		this.sendAttempts = sendAttempts;
	}

	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return MSystem.toString(this, getId(), source, to, subject);
	}

}
