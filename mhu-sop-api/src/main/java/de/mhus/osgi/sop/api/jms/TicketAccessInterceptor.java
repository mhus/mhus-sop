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
package de.mhus.osgi.sop.api.jms;

import java.util.Locale;

import javax.jms.JMSException;
import javax.jms.Message;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.jms.JmsInterceptor;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public class TicketAccessInterceptor extends MLog implements JmsInterceptor {

	public static final CfgString TICKET_KEY = new CfgString(JmsApi.class, "aaaTicketName", "mhus.ticket");
	public static final CfgString LOCALE_KEY = new CfgString(JmsApi.class, "aaaLocaleName", "mhus.locale");
	public static final CfgBoolean RELAXED = new CfgBoolean(JmsApi.class, "aaaRelaxed", true);
	
	@Override
	public void begin(Message message) {
		String ticket;
		try {
			ticket = message.getStringProperty(TICKET_KEY.value());
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		try {
			AccessApi api = MApi.lookup(AccessApi.class);
			if (api == null) {
				if (RELAXED.value()) 
					return;
				else
					throw new AccessDeniedException("access api not found");
			}
			String localeStr = message.getStringProperty(LOCALE_KEY.value());
			Locale locale = localeStr == null ? null : Locale.forLanguageTag(localeStr);
			if (ticket == null)
				api.process(api.getGuestAccount(),null,false, locale);
			else
				api.process(ticket, locale);
		} catch (Throwable t) {
			log().i("Incoming Access Denied",message);
			throw new RuntimeException(t);
		}
	}

	@Override
	public void end(Message message) {
		
		AccessApi api = MApi.lookup(AccessApi.class);
		if (api == null) return;

		String ticket;
		try {
			ticket = message.getStringProperty(TICKET_KEY.value());
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		if (ticket == null)
			MApi.lookup(AccessApi.class).release(api.getGuestContext());
		else
			MApi.lookup(AccessApi.class).release(ticket);
	}

	@Override
	public void prepare(Message answer) {

		
	}

	@Override
	public void answer(Message message) {
		
	}

}
