package de.mhus.osgi.sop.jms.operation;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MLog;
import de.mhus.osgi.sop.api.registry.RegistryProvider;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(immediate=true)
public class JmsRegistryProvider extends MLog implements RegistryProvider {

	
	@Override
	public void publish(RegistryValue entry) {
		JmsApiImpl.instance.registryPublish(entry);
	}

	@Override
	public void remove(String path) {
		JmsApiImpl.instance.registryRemove(path);
	}

	@Override
	public void publishAll() {
		JmsApiImpl.instance.sendLocalOperations();
	}

	@Override
	public void requestAll() {
		JmsApiImpl.instance.requestRegistry();		
	}

}
