package de.mhus.osgi.sop.jms.operation;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MLog;
import de.mhus.osgi.sop.api.registry.RegistryProvider;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(immediate=true)
public class JmsRegistryProvider extends MLog implements RegistryProvider {

	
	@Override
	public boolean publish(RegistryValue entry) {
		return JmsApiImpl.instance.registryPublish(entry);
	}

	@Override
	public boolean remove(String path) {
		return JmsApiImpl.instance.registryRemove(path);
	}

	@Override
	public boolean publishAll() {
		return JmsApiImpl.instance.sendLocalRegistry();
	}

	@Override
	public boolean requestAll() {
		return JmsApiImpl.instance.requestRegistry();		
	}

}
