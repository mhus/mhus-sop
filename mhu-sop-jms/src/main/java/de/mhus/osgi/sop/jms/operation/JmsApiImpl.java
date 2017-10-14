package de.mhus.osgi.sop.jms.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.ServerJms;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;

public class JmsApiImpl extends MLog implements JmsApi {

	public static CfgString connectionName = new CfgString(JmsApi.class, "connection", "sop");
	protected static JmsApiImpl instance;

	private ClientJms registerClient;
	private ServerJms registerServer;
	HashMap<String, JmsOperationDescriptor> register = new HashMap<>();
	private TimerIfc timer;
	protected long lastRegistryRequest;


	@Override
	public String getDefaultConnectionName() {
		return connectionName.value();
	}

	@Override
	public void sendLocalOperations() {
		try {
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "operations");
			msg.setStringProperty("connection", MApi.lookup(JmsApi.class).getDefaultConnectionName());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			
			int cnt = 0;
			
			for ( OperationDescriptor desc : MApi.lookup(OperationApi.class).findOperations("*", null, null)) {
				if (!JmsOperationApiImpl.PROVIDER_NAME.equals(desc.getProvider())) {
					msg.setString("operation" + cnt, desc.getPath());
					msg.setString("version" + cnt, desc.getVersionString());
					msg.setString("tags" + cnt, MString.join(desc.getTags().iterator(), ",") );
					msg.setString("title" + cnt, desc.getTitle());
					// TODO msg.setString("form" + cnt, desc.getForm());
					cnt++;
				}
			}
			
			registerClient.sendJms(msg);
		} catch (Throwable t) {
			log().w(t);
		}
	}

	@Override
	public void requestOperationRegistry() {
		try {
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "request");
			msg.setStringProperty("connection", MApi.lookup(JmsApi.class).getDefaultConnectionName());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			registerClient.sendJms(msg);
		} catch (Throwable t) {
			log().w(t);
		}
	}

	@Activate
	public void doActivate(ComponentContext ctx) {
		instance = this;
		registerClient = new ClientJms(new JmsDestination(JmsApi.REGISTRY_TOPIC, true));
		registerServer = new ServerJms(new JmsDestination(JmsApi.REGISTRY_TOPIC, true)) {
			
			@Override
			public void receivedOneWay(Message msg) throws JMSException {
				if (msg instanceof MapMessage && 
					!Jms2LocalOperationExecuteChannel.queueName.value().equals(msg.getStringProperty("queue")) // do not process my own messages
				   ) {
					
					MapMessage m = (MapMessage)msg;
					String type = m.getStringProperty("type");
					if ("request".equals(type) ) {
						lastRegistryRequest = System.currentTimeMillis();
						sendLocalOperations();
					}
					if ("operations".equals("type")) {
						String queue = m.getStringProperty("queue");
						String connection = getDefaultConnectionName(); //TODO
						int cnt = 0;
						String path = null;
						synchronized (register) {
							long now = System.currentTimeMillis();
							do {
								path = m.getString("operation" + cnt);
								String version = m.getString("version" + cnt);
								String tags = m.getString("tags" + cnt);
								String form = m.getString("form" + cnt);
								cnt++;
								String ident = connection + "," + queue + "," + path + "," + version;
								JmsOperationDescriptor desc = register.get(ident);
								if (desc == null) {
									OperationAddress a = new OperationAddress(JmsOperationApiImpl.PROVIDER_NAME + "://" + path + ":" + version + "/" + queue + "/" + connection);
									OperationDescription d = new OperationDescription(a.getGroup(),a.getName(),a.getVersion(),null,null);
									// TODO transform string to form
									// d.setForm(form);
									desc = new JmsOperationDescriptor(a,d,tags == null ? null : MCollection.toList(tags.split(",")));
									register.put(ident, desc);
								}
								desc.setLastUpdated();
							} while (path != null);
							// remove stare
							register.entrySet().removeIf(
									entry -> entry.getValue().getAddress().getPart(0).equals(queue) && 
											 entry.getValue().getLastUpdated() < now
									);
						}
					}
				}
			}
			
			@Override
			public Message received(Message msg) throws JMSException {
				return null;
			}
		};
		
		timer = MApi.lookup(TimerFactory.class).getTimer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				doCheckRegistry();
			}
			
		}, MTimeInterval.MINUTE_IN_MILLISECOUNDS);
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance = null;
		if (timer != null)
			timer.cancel();
		timer = null;
		
		if (registerClient != null)
			registerClient.close();
		registerClient = null;
		if (registerServer != null)
			registerServer.close();
		registerServer = null;
		register.clear();
		
	}

	protected void doCheckRegistry() {
		if (MTimeInterval.isTimeOut(lastRegistryRequest,MTimeInterval.MINUTE_IN_MILLISECOUNDS * 3)) {
			long now = System.currentTimeMillis();
			requestOperationRegistry();
			sendLocalOperations();
			MThread.sleep(30000);
			
			// remove staled - if not updated in the last 30 seconds
			synchronized (register) {
				register.entrySet().removeIf(e -> e.getValue().getLastUpdated() < now);
			}
		}
	}

	public class JmsOperationDescriptor extends OperationDescriptor {

		private long lastUpdated;

		public JmsOperationDescriptor(OperationAddress address, OperationDescription description,
				Collection<String> tags) {
			super(address, description, tags);
		}

		public long getLastUpdated() {
			return lastUpdated;
		}

		public void setLastUpdated() {
			lastUpdated = System.currentTimeMillis();
		}
		
	}

}
