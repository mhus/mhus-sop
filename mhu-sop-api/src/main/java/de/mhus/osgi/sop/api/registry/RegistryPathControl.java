package de.mhus.osgi.sop.api.registry;

public interface RegistryPathControl {
	
	/**
	 * Return true if the path is controlled by this controller. This is additional to
	 * checked to the component parameter 'path'. If you not have any special rule
	 * to ignore control you can return true by default.
	 * 
	 * @param path Path of the Node
	 * 
	 * @return
	 */
	boolean isTakeControl(String path);

	/**
	 * The method can modify or deny setting of the parameter. If not implemented
	 * return the incoming value.
	 * 
	 * @param manager
	 * @param value 
	 * @return The value, modified value or null to deny setting.
	 */
	RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value);

	/**
	 * The method can deny removal of a parameter.
	 * 
	 * @param manager
	 * @param value
	 * @return false to deny removal
	 */
	boolean checkRemoveParameter(RegistryManager manager, RegistryValue value);

	/**
	 * The method can modify or deny local setting of the parameter. If not implemented
	 * return the incoming value.
	 * 
	 * @param manager
	 * @param value
	 * @return The value, modified value or null to deny setting.
	 */
	RegistryValue checkSetParameterFromRemote(RegistryManager manager, RegistryValue value);

	/**
	 * The method can deny local removal of a parameter.
	 * 
	 * @param manager
	 * @param value
	 * @return false to deny removal.
	 */
	boolean checkRemoveParameterFromRemote(RegistryManager manager, RegistryValue value);

}
