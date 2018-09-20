package de.mhus.osgi.sop.api.mailqueue;

public class MailMessage {

	private String source;
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String content;
	String[] attachments;
	private boolean individual = true;
	
	public MailMessage(String source, String from, String to, String cc, String bcc, String subject,
	        String content, String[] attachments, boolean individual) {
		super();
		this.source = source;
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.content = content;
		this.attachments = attachments;
		this.individual = individual;
	}
	
	public String getSource() {
		return source;
	}
	public String getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public String getCc() {
		return cc;
	}
	public String getBcc() {
		return bcc;
	}
	public String getSubject() {
		return subject;
	}
	public String getContent() {
		return content;
	}
	public String[] getAttachments() {
		return attachments;
	}
	public boolean isIndividual() {
		return individual;
	}

	/**
	 * Split a individual (every receiver gets his own message) into separate MailMessages with one receiver
	 * @return List of Messages
	 */
	public MailMessage[] getSeparateMails() {
		if (!individual || to.indexOf(';') < 0) return new MailMessage[] {this};
		String[] toList = to.split(";");
		MailMessage[] out = new MailMessage[toList.length];
		for (int i = 0; i < out.length; i++)
			out[i] = new MailMessage(source, from, toList[i], cc, bcc, subject, content, toList, individual);
		return out;
	}
	
}
