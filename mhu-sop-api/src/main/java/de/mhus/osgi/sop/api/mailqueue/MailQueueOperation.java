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
package de.mhus.osgi.sop.api.mailqueue;

import java.util.Date;
import java.util.UUID;

import de.mhus.lib.errors.MException;

public interface MailQueueOperation {

	enum STATUS {NEW, READY, SENT, ERROR, ERROR_PREPARE, LOST};

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
	
	/**
	 * Return the last send attempt. In case of success the time of
	 * time of sent.
	 * @param id 
	 * @return The date or null if not set
	 * @throws MException Throws if id is not found
	 */
	Date getLastSendAttempt(UUID id) throws MException;
	
}
