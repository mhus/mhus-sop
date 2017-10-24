package de.mhus.osgi.sop.api.jms;

import de.mhus.lib.karaf.jms.JmsManagerService;

public interface JmsApi {

	public static final String OPT_FORCE_MAP_MESSAGE = "forceMapMessage";
	public static final String REGISTRY_TOPIC = "sop.registry";
	public static final String OPT_TIMEOUT = "timeout";
	public static final String OPT_ONE_WAY = "oneWay";

	String getDefaultConnectionName();

	void sendLocalOperations();
	
	void requestOperationRegistry();

}
