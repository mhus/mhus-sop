package de.mhus.osgi.sop.jms.operation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimerTask;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.osgi.service.component.ComponentContext;

import com.vaadin.data.util.GeneratedPropertyContainer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationException;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.operation.OperationsProvider;
import de.mhus.osgi.sop.api.util.ObjectUtil;
import de.mhus.osgi.sop.jms.operation.JmsApiImpl.JmsOperationDescriptor;

@Component(immediate=true,properties="provider=jms")
public class JmsOperationProvider extends MLog implements OperationsProvider {

	protected static final String PROVIDER_NAME = "jms";

	@Activate
	public void doActivate(ComponentContext ctx) {
		
	}


	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		
	}


	@Override
	public void collectOperations(List<OperationDescriptor> list, String filter, VersionRange version,
			Collection<String> providedTags) {
		synchronized (JmsApiImpl.instance.register) {
			HashMap<String, JmsOperationDescriptor> register = JmsApiImpl.instance.register;
			for (JmsOperationDescriptor desc : register.values())
				if (OperationUtil.matches(desc, filter, version, providedTags))
					list.add(desc);
		}
	}


	@Override
	public OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags,
			IProperties properties, String... executeOptions) throws NotFoundException {
		OperationDescriptor d = null;
		synchronized (JmsApiImpl.instance.register) {
			HashMap<String, JmsOperationDescriptor> register = JmsApiImpl.instance.register;
			for (OperationDescriptor desc : register.values()) {
				if (OperationUtil.matches(desc, filter, version, providedTags) ) {
						d = desc;
						break;
				}
			}
		}
		if (d == null) throw new NotFoundException("operation not found",filter,version,providedTags);
		return doExecute(d, properties);
	}


	@Override
	public OperationResult doExecute(OperationDescriptor desc, IProperties properties, String... executeOptions)
			throws NotFoundException {

		if (!PROVIDER_NAME.equals(desc.getProvider()))
			throw new NotFoundException("description is from another provider",desc);

		String conName = desc.getAddress().partSize() > 1 ? desc.getAddress().getPart(1) : JmsApiImpl.instance.getDefaultConnectionName();
		String queueName = desc.getAddress().getPart(0);
		String path = desc.getPath();
		String version = desc.getVersionString();
		
		JmsConnection con = JmsUtil.getConnection(conName);
		
		AccessApi api = MApi.lookup(AccessApi.class);
		
		String ticket = api == null ? null : api.createTrustTicket(api.getCurrent()); // TODO Configurable via execute options
		long timeout = OperationUtil.getOption(executeOptions, JmsApi.OPT_TIMEOUT, MTimeInterval.MINUTE_IN_MILLISECOUNDS); // TODO Configurable via execute options
		
		try {
			return doExecuteOperation(con, queueName, path, version, properties, ticket, timeout, executeOptions);
		} catch (Exception e) {
			return new NotSuccessful(path, e.getMessage(), OperationResult.INTERNAL_ERROR);
		}
		
	}

	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, String version, IProperties parameters, String ticket, long timeout, String ... options  ) throws Exception {

		if (con == null) throw new JMSException("connection is null");
		ClientJms client = new ClientJms(con.createQueue(queueName));
		
		boolean needObject = false;
		if (!OperationUtil.isOption(options, JmsApi.OPT_FORCE_MAP_MESSAGE)) {
			for (Entry<String, Object> item : parameters) {
				Object value = item.getValue();
				if (! (
						value == null || 
						value.getClass().isPrimitive() || 
						value instanceof String || 
						value instanceof Long || 
						value instanceof Integer || 
						value instanceof Boolean 
					) ) {
					needObject = true;
					break;
				}
			}
		}
		
		Message msg = null;
		if (needObject) {
			msg = con.createObjectMessage((MProperties)parameters);
		} else {
			msg = con.createMapMessage();
			for (Entry<String, Object> item : parameters) {
				String name = item.getKey();
				//if (!name.startsWith("_"))
				Object value = item.getValue();
				if (value != null && value instanceof Date) 
					value = MDate.toIsoDateTime((Date)value);
				else
				if (value != null && 
					!(value instanceof String) && !value.getClass().isPrimitive() ) 
					value = String.valueOf(value);
				((MapMessage)msg).setObject(name, value);
			}
			((MapMessage)msg).getMapNames();
		}
		
		msg.setStringProperty(Sop.PARAM_OPERATION_PATH, operationName);
		msg.setStringProperty(Sop.PARAM_OPERATION_VERSION, version);


		msg.setStringProperty(Sop.PARAM_AAA_TICKET, ticket );
		client.setTimeout(timeout);
    	// Send Request
    	
    	log().d(operationName,"sending Message", queueName, msg, options);
    	
    	if (OperationUtil.isOption(options,JmsApi.OPT_ONE_WAY)) {
    		client.sendJmsOneWay(msg);
    		return null;
    	}
    	
    	Message answer = client.sendJms(msg);

    	// Process Answer
    	
    	OperationResult out = new OperationResult();
    	out.setOperationPath(operationName);
		if (answer == null) {
			log().d(queueName,operationName,"answer is null");
			out.setSuccessful(false);
			out.setMsg("answer is null");
			out.setReturnCode(OperationResult.INTERNAL_ERROR);
		} else {
			// remote error handling
			String errorMsg = answer.getStringProperty(Sop.PARAM_ERROR);
			if (errorMsg != null) {
				throw new MException("Remote error",errorMsg);
			}
			
			// check if technical successful
			boolean successful = answer.getBooleanProperty(Sop.PARAM_SUCCESSFUL);
			out.setSuccessful(successful);
			
			if (!successful)
				out.setMsg(answer.getStringProperty(Sop.PARAM_MSG));
			out.setReturnCode(answer.getLongProperty(Sop.PARAM_RC));
			
			// if (successful) { // also errors can have a result object
				
				if (answer instanceof MapMessage) {
					MapMessage mapMsg = (MapMessage)answer;
					out.setResult(MJms.getMapProperties(mapMsg));
				} else
				if (answer instanceof TextMessage) {
					out.setMsg(((TextMessage)answer).getText());
					out.setResult(out.getMsg());
				} else
				if (answer instanceof BytesMessage) {
					long len = ((BytesMessage)answer).getBodyLength();
					if (len > Sop.MAX_MSG_BYTES) {
						out.setMsg("answer bytes too long " + len);
						out.setSuccessful(false);
						out.setReturnCode(OperationResult.INTERNAL_ERROR);
					} else {
						byte[] bytes = new byte[(int) len];
						((BytesMessage)answer).readBytes(bytes);
						out.setResult(bytes);
					}
				} else
				if (answer instanceof ObjectMessage) {
					Serializable obj = ((ObjectMessage)answer).getObject();
					if (obj == null) {
						out.setResult(null);
					} else {
						out.setResult(obj);
					}
				}
			// }	
		}
		
		
		client.close();
		
		return out;
	}

	@Override
	public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
		String connection = JmsApiImpl.instance.getDefaultConnectionName(); //TODO
		String queue = addr.getPart(0);
		String path = addr.getPath();
		String version = addr.getVersionString();
		String ident = connection + "," + queue + "," + path + "," + version;
		synchronized (JmsApiImpl.instance.register) {
			JmsOperationDescriptor res = JmsApiImpl.instance.register.get(ident);
			if (res == null) throw new NotFoundException("operation not found",addr);
			return res;
		}
	}


	@Override
	public void synchronize() {
		// TODO time configurable
		if (MTimeInterval.isTimeOut(JmsApiImpl.instance.lastRegistryRequest,MTimeInterval.MINUTE_IN_MILLISECOUNDS * 3)) {
			long now = System.currentTimeMillis();
			JmsApiImpl.instance.requestOperationRegistry();
			MThread.sleep(30000);
			
			// remove staled - if not updated in the last 30 seconds
			synchronized (JmsApiImpl.instance.register) {
				JmsApiImpl.instance.register.entrySet().removeIf(e -> e.getValue().getLastUpdated() < now);
			}
		}

	}
		
}
