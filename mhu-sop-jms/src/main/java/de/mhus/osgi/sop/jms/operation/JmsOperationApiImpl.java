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
import de.mhus.osgi.sop.api.operation.OperationsProvider;
import de.mhus.osgi.sop.jms.operation.JmsApiImpl.JmsOperationDescriptor;

@Component(immediate=true,properties="provider=jms")
public class JmsOperationApiImpl extends MLog implements OperationsProvider {

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
				if (MString.compareFsLikePattern(desc.getPath(), filter) && 
						version.includes(desc.getVersion()) && 
						(providedTags == null || desc.compareTags(providedTags)) )
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
				if (MString.compareFsLikePattern(desc.getPath(), filter) && 
					version.includes(desc.getVersion()) && 
					(providedTags == null || desc.compareTags(providedTags)) ) {
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
		String path = desc.getPath() + ":" + desc.getVersionString();
		
		JmsConnection con = JmsUtil.getConnection(conName);
		
		AccessApi api = MApi.lookup(AccessApi.class);
		
		String ticket = api == null ? null : api.createTrustTicket(api.getCurrent()); // TODO Configurable via execute options
		long timeout = MTimeInterval.MINUTE_IN_MILLISECOUNDS; // TODO Configurable via execute options
		
		try {
			return doExecuteOperation(con, queueName, path, properties, ticket, timeout, executeOptions);
		} catch (Exception e) {
			return new NotSuccessful(path, e.getMessage(), OperationResult.INTERNAL_ERROR);
		}
		
	}

	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, String ticket, long timeout, String ... options  ) throws Exception {

		if (con == null) throw new JMSException("connection is null");
		ClientJms client = new ClientJms(con.createQueue(queueName));
		
		boolean needObject = false;
		if (!isOption(options, JmsApi.OPT_FORCE_MAP_MESSAGE)) {
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


		msg.setStringProperty(Sop.PARAM_AAA_TICKET, ticket );
		client.setTimeout(timeout);
    	// Send Request
    	
    	log().d(operationName,"sending Message", queueName, msg, options);
    	
    	if (!isOption(options,JmsApi.OPT_NEED_ANSWER)) {
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
			boolean successful = answer.getBooleanProperty(Sop.PARAM_SUCCESSFUL);
			out.setSuccessful(successful);
			
			if (!successful)
				out.setMsg(answer.getStringProperty(Sop.PARAM_MSG));
			out.setReturnCode(answer.getLongProperty(Sop.PARAM_RC));
			
			if (successful) {
				
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
			}	
		}
		
		
		client.close();
		
		return out;
	}
		
	private boolean isOption(String[] options, String opt) {
		if (options == null || opt == null) return false;
		for (String o : options)
			if (opt.equals(o)) return true;
		return false;
	}
//
//	@Override
//	public List<String>	getOperationList(JmsConnection con, String queueName, AaaContext user) throws Exception {
//		IProperties pa = new MProperties();
//		OperationResult ret = doExecuteOperation(con, queueName, "_list", pa, user, OPT_NEED_ANSWER);
//		if (ret.isSuccessful()) {
//			Object res = ret.getResult();
//			if (res != null && res instanceof MProperties) {
//				String[] list = String.valueOf( ((MProperties)res).getString("list","") ).split(",");
//				LinkedList<String> out = new LinkedList<String>();
//				for (String item : list) out.add(item);
//				return out;
//			}
//		}
//		return null;
//	}
//	
//	@Override
//	public List<String> lookupOperationQueues() throws Exception {
//		JmsConnection con = JmsUtil.getConnection(OperationBroadcast.connectionName.value());
//		ClientJms client = new ClientJms(con.createTopic(OperationBroadcast.queueName.value()));
//		client.open();
//		TextMessage msg = client.getSession().createTextMessage();
//		LinkedList<String> out = new LinkedList<String>();
//		for (Message ret : client.sendJmsBroadcast(msg)) {
//			String q = ret.getStringProperty("queue");
//			if (q != null)
//				out.add(q);
//		}
//		return out;
//	}
//

//	@Override
//	public List<OperationAddress> getRegisteredOperations() {
//		synchronized (register) {
//			return new LinkedList<OperationAddress>( register.values() );
//		}
//	}
//
//	@Override
//	public OperationAddress getRegisteredOperation(String path, VersionRange version) {
//		synchronized (register) {
//			for (OperationAddress r : register.values())
//				if (r.getPath().equals(path) && version == null || version.includes(r.getVersion()))
//					return r;
//		}
//		return null;
//	}
		
}
