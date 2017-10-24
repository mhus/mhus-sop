package de.mhus.osgi.sop.api.jms;

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
			if (ticket == null)
				api.process(api.getGuestAccount(),null,false);
			else
				api.process(ticket);
		} catch (Throwable t) {
			log().i("Incoming Access Denied",message);
			throw t;
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
