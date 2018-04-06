package de.mhus.osgi.sop.api.mailqueue;

import java.util.UUID;

import de.mhus.lib.errors.MException;

public interface MailQueueOperation {

	enum STATUS {NEW, READY, SENT, ERROR, ERROR_PREPARE};

	/**
	 * Will schedule the given mail content as html mail. And send a separate mail
	 * to every recipient.
	 * @param source 
	 * 
	 * @param from From address or null for the default sender address
	 * @param to Minimum set one recipient address.
	 * @param subject The subject as string
	 * @param content The content as string or the File Queue uri starting with "dfq:"
	 * @param attachments List of File Queue URIs to load the attachments
	 * @return The first task id of the created tasks
	 * @throws MException 
	 */
	UUID scheduleHtmlMail(String source, String from, String[] to, String subject, String content, String ... attachments ) throws MException;

	/**
	 * Return the send status of the mail
	 * @param id
	 * @return The status
	 * @throws MException
	 */
	STATUS getStatus(UUID id) throws MException;
	
}
