package de.mhus.osgi.sop.api.registry;

import java.util.Collection;

public interface RegistryManager extends RegistryApi {

	/**
	 * Set or update locally the value.
	 * 
	 * @param value
	 */
	void setParameterFromRemote(RegistryValue value);

	/**
	 * Remove the parameter from local registry
	 * @param path
	 * @param source 
	 */
	void removeParameterFromRemote(String path, String source);

	Collection<RegistryValue> getAll();

	RegistryPathControl getPathController(String path);
}
