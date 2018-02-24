package de.mhus.osgi.sop.api.registry;

import de.mhus.lib.core.MLog;

public class RegistryPathControlAdapter extends MLog implements RegistryPathControl {

	@Override
	public boolean isTakeControl(String path) {
		return true;
	}

	@Override
	public RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value) {
		return value;
	}

	@Override
	public boolean checkRemoveParameter(RegistryManager manager, RegistryValue value) {
		return true;
	}

	@Override
	public RegistryValue checkSetParameterFromRemote(RegistryManager manager, RegistryValue value) {
		return value;
	}

	@Override
	public boolean checkRemoveParameterFromRemote(RegistryManager manager, RegistryValue value) {
		return true;
	}

}
