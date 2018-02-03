package de.mhus.osgi.sop.api.registry;

public interface RegistryPathControl {

	RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value);

	boolean checkRemoveParameter(RegistryManager manager, RegistryValue value);

	RegistryValue checkSetLocalParameter(RegistryManager manager, RegistryValue value);

	boolean checkRemoveLocalParameter(RegistryManager manager, RegistryValue value);

}
