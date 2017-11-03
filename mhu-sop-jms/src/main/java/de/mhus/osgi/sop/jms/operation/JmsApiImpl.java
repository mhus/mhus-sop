package de.mhus.osgi.sop.jms.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.osgi.service.component.ComponentContext;
import org.w3c.dom.Document;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.service.ServerIdent;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.ServerJms;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;

@Component(immediate=true)
public class JmsApiImpl extends MLog implements JmsApi {

	public static CfgString connectionName = new CfgString(JmsApi.class, "connection", "sop");
	protected static JmsApiImpl instance;

	private ClientJms registerClient;
	HashMap<String, JmsOperationDescriptor> register = new HashMap<>();
	long lastRegistryRequest;

	@Override
	public String getDefaultConnectionName() {
		return connectionName.value();
	}

	@Override
	public void sendLocalOperations() {
		try {
			checkClient();
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "operations");
			msg.setStringProperty("connection", MApi.lookup(JmsApi.class).getDefaultConnectionName());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			
			int cnt = 0;
			
			for ( OperationDescriptor desc : MApi.lookup(OperationApi.class).findOperations("*", null, null)) {
				if (!JmsOperationProvider.PROVIDER_NAME.equals(desc.getProvider())) {
					msg.setString("operation" + cnt, desc.getPath());
					msg.setString("version" + cnt, desc.getVersionString());
					String tags = MString.join(desc.getTags().iterator(), ",");
					if (tags.length() > 0) tags = tags + ",";
					tags = tags + "remote:jms,host:" + MSystem.getHostname() + ",ident:" + MApi.lookup(ServerIdent.class).toString();
					msg.setString("tags" + cnt, tags );
					msg.setString("title" + cnt, desc.getTitle());
					DefRoot form = desc.getForm();
					if (form != null) {
						Document doc = ModelUtil.toXml(form);
						msg.setString("form" + cnt, MXml.toString(doc.getDocumentElement(), false));
					}
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
			checkClient();
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "request");
			msg.setStringProperty("connection", MApi.lookup(JmsApi.class).getDefaultConnectionName());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			registerClient.sendJmsOneWay(msg);
		} catch (Throwable t) {
			log().w(t);
		}
	}

	private void checkClient() {
		if (registerClient.getJmsDestination().getConnection() == null) {
			JmsConnection con = JmsUtil.getConnection( getDefaultConnectionName() );
			if (con != null)
				registerClient.getJmsDestination().setConnection(con);
		}
	}

	@Activate
	public void doActivate(ComponentContext ctx) {
		instance = this;
		registerClient = new ClientJms(new JmsDestination(JmsApi.REGISTRY_TOPIC, true));
		
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance = null;
		
		if (registerClient != null)
			registerClient.close();
		registerClient = null;
		register.clear();
		
	}

	public static class JmsOperationDescriptor extends OperationDescriptor {

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
