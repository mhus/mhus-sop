package de.mhus.osgi.sop.api.registry;

import java.io.IOException;
import java.util.Set;

public interface RegistryApi {

	public static final String PATH_SYSTEM = "/system/"; // this path will not fire events to the cfg system
	public static final String PATH_LOCAL = "/local/"; // this path will not be published
	
	/**
	 * Return the value information of a node parameter.
	 * 
	 * @param path Node path and parameter name e.g. '/node1/node2@parameter'
	 * @return The value or null if not found
	 */
	RegistryValue getNodeParameter(String path);
	
	/**
	 * Returns a list of children nodes.
	 * 
	 * @param path Path to the node where to request the children.
	 * @return List of node names or an empty list.
	 */
	Set<String> getNodeChildren(String path);
	
	/**
	 * Returns a list of parameters of the given node.
	 * 
	 * @param path Path to the node where to request the parameters.
	 * @return List of parameter names.
	 */
	Set<String> getParameterNames(String path);

	/**
	 * Set and publish a value for the given parameter.
	 * @param path Node path and parameter name e.g. '/node1/node2@parameter'
	 * @param value New value
	 * @param timeout Timeout in milliseconds after receive or 0
	 * @param readOnly Readonly can only be updated by the source
	 * @param persistent Set the possibility that the value will be stored (currently only local)
	 * @return true if the value was really changed or false if the value was the same as before
	 */
	boolean setParameter(String path, String value, long timeout, boolean readOnly, boolean persistent);

	default boolean setParameter(String path, String value) {
		return setParameter(path, value, 0, true, false);
	}
	
	/**
	 * Remove a node or parameter. And publish the information.
	 * 
	 * @param path
	 */
	boolean removeParameter(String path);

	Set<RegistryValue> getParameters(String path);

	void publishAll();

	void requestAll();

	void save() throws IOException;

	void load();
	
}
