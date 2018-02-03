package de.mhus.osgi.sop.api.registry;

import java.util.Collection;
import java.util.List;

public interface RegistryManager extends RegistryApi {

	/**
	 * Set or update locally the value.
	 * 
	 * @param value
	 */
	void setLocalParameter(RegistryValue value);

	/**
	 * Remove the parameter from local registry
	 * @param path
	 * @param source 
	 */
	void removeLocalParameter(String path, String source);

	Collection<RegistryValue> getAll();

	RegistryPathControl getPathController(String path);
}
