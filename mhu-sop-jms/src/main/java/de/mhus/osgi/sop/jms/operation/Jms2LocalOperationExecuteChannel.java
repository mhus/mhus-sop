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
package de.mhus.osgi.sop.jms.operation;

import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;

import org.osgi.service.component.ComponentContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.service.ServerIdent;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.ServerJms;
import de.mhus.osgi.services.jms.JmsDataChannel;
import de.mhus.osgi.sop.api.jms.AbstractJmsOperationExecuteChannel;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.jms.TicketAccessInterceptor;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;

@Component(service=JmsDataChannel.class,immediate=true)
public class Jms2LocalOperationExecuteChannel extends AbstractJmsOperationExecuteChannel {

	public static CfgString queueName = new CfgString(Jms2LocalOperationExecuteChannel.class, "queue", "sop.operation." + M.l(ServerIdent.class));
	static Jms2LocalOperationExecuteChannel instance;
	private JmsApi jmsApi;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		instance = this;
	}	
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance = null;
	}

	@Override
	protected JmsChannel createChannel() throws JMSException {
		JmsChannel out = super.createChannel();
		if (out != null && MApi.getCfg(Jms2LocalOperationExecuteChannel.class).getBoolean("accessControl", true))
			((ServerJms)out).setInterceptorIn(new TicketAccessInterceptor());
		return out;
	}
	
	@Override
	protected String getQueueName() {
		return  queueName.value();
	}

	@Reference
	public void setJmsApi(JmsApi api) {
		this.jmsApi = api;
	}
	
	@Override
	protected String getJmsConnectionName() {
		return jmsApi.getDefaultConnectionName();
		//return "sop";
	}

	@Override
	protected OperationResult doExecute(String path, VersionRange version, IProperties properties) throws NotFoundException {

		log().d("execute operation",path,properties);
		
		OperationApi api = M.l(OperationApi.class);
		OperationResult res = api.doExecute(path, version, null, properties, OperationApi.LOCAL_ONLY );
		
		log().d("operation result",path,res, res == null ? "" : res.getResult());
		return res;
	}

	@Override
	protected List<String> getPublicOperations() {
		LinkedList<String> out = new LinkedList<String>();
		OperationApi admin = M.l(OperationApi.class);
		for (OperationDescriptor desc :  admin.findOperations("*", null, null)) {
			if (!JmsOperationProvider.PROVIDER_NAME.equals(desc.getProvider())) {
				try {
					out.add(desc.getPath() + ":" + desc.getVersionString());
				} catch (Throwable t) {
					log().d(desc,t);
				}
			}
		}
			
		return out;
	}

	@Override
	protected OperationDescriptor getOperationDescription(String path, VersionRange version) throws NotFoundException {
		OperationApi admin = M.l(OperationApi.class);
		OperationDescriptor desc = admin.findOperation(path, version, null);
		if (desc == null) return null;
		return desc;
	}

	@Override
	public String getConnectionName() {
		connectionName = jmsApi.getDefaultConnectionName();
		return connectionName;
	}

}
