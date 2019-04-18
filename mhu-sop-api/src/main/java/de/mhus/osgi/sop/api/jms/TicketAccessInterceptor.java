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

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.jms.JmsInterceptor;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.util.SopUtil;

public class TicketAccessInterceptor extends MLog implements JmsInterceptor {

//	public static final CfgString TICKET_KEY = new CfgString(JmsApi.class, "aaaTicketParameter", "mhus.ticket");
//	public static final CfgString LOCALE_KEY = new CfgString(JmsApi.class, "aaaLocaleParameter", "mhus.locale");
	public static final CfgBoolean RELAXED = new CfgBoolean(JmsApi.class, "aaaRelaxed", true);
	
	@Override
	public void begin(Message message) {
		String ticket;
		try {
			ticket = message.getStringProperty(Sop.PARAM_AAA_TICKET);
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		try {
			AccessApi api = M.l(AccessApi.class);
			if (api == null) {
				if (RELAXED.value()) 
					return;
				else
					throw new AccessDeniedException("access api not found");
			}
			String localeStr = message.getStringProperty(Sop.PARAM_LOCALE);
			Locale locale = localeStr == null ? null : Locale.forLanguageTag(localeStr);
			if (ticket == null)
				api.process(api.getGuestAccount(),null,false, locale);
			else
				api.process(ticket, locale);
		} catch (Throwable t) {
			log().d("Incoming Access Denied",message);
			throw new RuntimeException(t);
		}
	}

	@Override
	public void end(Message message) {
		
		AccessApi api = M.l(AccessApi.class);
		if (api == null) return;

		String ticket;
		try {
			ticket = message.getStringProperty(Sop.PARAM_AAA_TICKET);
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		if (ticket == null)
			M.l(AccessApi.class).release(api.getGuestContext());
		else
			M.l(AccessApi.class).release(ticket);
	}

	@Override
	public void prepare(Message message) {

		AccessApi api = M.l(AccessApi.class);
		if (api == null) return;
		
		AaaContext current = api.getCurrent();
		if (current == null) return;
			
		String ticket = api.createTrustTicket(SopUtil.TRUST_NAME.value(), current);
		Locale l = current.getLocale();
		if (l == null) l = Locale.getDefault();
		String locale = l.toString();
		try {
			message.setStringProperty(Sop.PARAM_AAA_TICKET, ticket);
			message.setStringProperty(Sop.PARAM_LOCALE, locale);
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		
	}

	@Override
	public void answer(Message message) {
		
	}

}
