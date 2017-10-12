package de.mhus.osgi.sop.jms.operation;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.osgi.sop.api.jms.JmsApi;

public class JmsApiImpl extends MLog implements JmsApi {

	public static CfgString connectionName = new CfgString(JmsApi.class, "connection", "sop");

	@Override
	public String getDefaultConnectionName() {
		return connectionName.value();
	}

}
