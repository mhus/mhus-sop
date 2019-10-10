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
package de.mhus.osgi.sop.impl.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.lang.Value;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.util.OperationResultProxy;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.operation.OperationsProvider;

@Component(immediate=true,service=OperationApi.class)
public class OperationApiImpl extends MLog implements OperationApi {

	private ServiceTracker<OperationsProvider,OperationsProvider> nodeTracker;
	private HashMap<String, OperationsProvider> register = new HashMap<>();
	private BundleContext context;
	public static OperationApiImpl instance;
	private TimerIfc timer;
	private MTimerTask timerTask;
	private CfgLong CFG_OPERATION_SYNC = new CfgLong(OperationApi.class, "syncInterval", 300000);

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, OperationsProvider.class, new MyServiceTrackerCustomizer() );
		nodeTracker.open(true);
		instance = this;
		
		MThread.asynchron(new Runnable() {
			
			@Override
			public void run() {
				MThread.sleep(10000);
				synchronize();
			}
		});

	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (timer != null)
			timer.cancel();
		
		instance  = null;
		context = null;
	}

	@Reference(service=TimerFactory.class,policy = ReferencePolicy.DYNAMIC,cardinality = ReferenceCardinality.OPTIONAL)
	public void addTimerFactory(TimerFactory factory) {
		log().i("create timer");
		timer = factory.getTimer();
		timerTask = new MTimerTask() {
			
			@Override
			public void doit() throws Exception {
				synchronize();
			}
		};
		timer.schedule(timerTask, CFG_OPERATION_SYNC.value(), MPeriod.MINUTE_IN_MILLISECOUNDS );
	}

   public void removeTimerFactory(TimerFactory factory) {
       
   }
   
	private class MyServiceTrackerCustomizer implements ServiceTrackerCustomizer<OperationsProvider,OperationsProvider> {

		@Override
		public OperationsProvider addingService(
				ServiceReference<OperationsProvider> reference) {

			OperationsProvider service = context.getService(reference);
			if (service != null) {
				String name = String.valueOf(reference.getProperty("provider"));
				log().i("register",name);
				synchronized (register) {
					OperationsProvider o = register.put(name, service);
					if (o != null)
						log().w("Provider was already registered",name);
				}
			}
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<OperationsProvider> reference,
				OperationsProvider service) {

			if (service != null) {
				String name = String.valueOf(reference.getProperty("provider"));
				log().i("modified",name);
				synchronized (register) {
					register.put(name,service);
				}
			}
			
		}

		@Override
		public void removedService(
				ServiceReference<OperationsProvider> reference,
				OperationsProvider service) {
			
			if (service != null) {
				String name = String.valueOf(reference.getProperty("provider"));
				log().i("unregister",name);
				synchronized (register) {
					register.remove(name);
				}
			}			
		}
		
	}

	public OperationsProvider getProvider(String name) {
		synchronized (register) {
			return register.get(name);
		}
	}
	
	@Override
	public String[] getProviderNames() {
		synchronized (register) {
			return register.keySet().toArray(new String[register.size()]);
		}
	}

	public OperationsProvider[] getProviders() {
		synchronized (register) {
			return register.values().toArray(new OperationsProvider[register.size()]);
		}
	}
	
	@Override
	public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
		OperationsProvider provider = getProvider(addr.getProvider());
		return provider.getOperation(addr);
	}
	
	@Override
	public List<OperationDescriptor> findOperations(String filter, VersionRange version,
			Collection<String> providedTags) {
		LinkedList<OperationDescriptor> list = new LinkedList<>();
		for (OperationsProvider provider : getProviders())
			try {
				provider.collectOperations(list, filter, version, providedTags);
			} catch (Throwable t) {
				log().d(filter,version,providedTags,t);
			}
		return list;
	}

	@Override
	public OperationDescriptor findOperation(String filter, VersionRange version, Collection<String> providedTags) throws NotFoundException {
		LinkedList<OperationDescriptor> list = new LinkedList<>();
		for (OperationsProvider provider : getProviders()) {
			try {
				provider.collectOperations(list, filter, version, providedTags);
			} catch (Throwable t) {
				log().d(filter,version,providedTags,t);
			}
			if (list.size() > 0)
				return list.getFirst();
		}
		throw new NotFoundException("operation not found",filter,version,providedTags);
	}

	@Override
	public OperationResult doExecute(String filter, VersionRange version, Collection<String> providedTags,
			IProperties properties, String... executeOptions) throws NotFoundException {
		
		if (OperationUtil.isOption(executeOptions, LOCAL_ONLY)) {
			synchronized (register) {
				OperationsProvider provider = register.get(OperationApi.DEFAULT_PROVIDER_NAME);
				return unwrap(provider.doExecute(filter, version, providedTags, properties, executeOptions), executeOptions);
			}
		} else {
			for (OperationsProvider provider : getProviders()) {
				try {
					return unwrap(provider.doExecute(filter, version, providedTags, properties, executeOptions), executeOptions);
				} catch (NotFoundException nfe) {}
			}
		}
		
		throw new NotFoundException("operation not found",filter,version,providedTags, executeOptions);
	}

	@Override
	public OperationResult doExecute(OperationDescriptor desc, IProperties properties, String ... executeOptions) throws NotFoundException {
		OperationsProvider provider = getProvider(desc.getProvider());
		if (provider == null) throw new NotFoundException("provider for operation not found",desc, executeOptions);
		return unwrap(provider.doExecute(desc, properties), executeOptions);
	}

	protected OperationResult unwrap(OperationResult res, String[] executeOptions) {
	    
	    if (OperationUtil.isOption(executeOptions, RAW_RESULT))
	        return res;
	    
        // unwrap result.result
        if (res != null && res.getResult() != null && res.getResult() instanceof Value) {
            OperationResultProxy wrap = new OperationResultProxy(res);
            wrap.setResult( ((Value<?>)res.getResult()).getValue() );
            res = wrap;
        }
        return res;
    }

    @Override
	public void synchronize() {
		for (OperationsProvider provider : getProviders()) {
			try {
				provider.synchronize();
			} catch (Throwable e) {
				log().d(provider,e);
			}
		}
	}

	@Override
	public void reset() {
		nodeTracker.close();
		nodeTracker.open(true);
	}

}
