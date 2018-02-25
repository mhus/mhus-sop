package de.mhus.osgi.sop.api.registry;

public interface RegistryProvider {

	boolean publish(RegistryValue entry);

	boolean remove(String path);

	boolean publishAll();

	boolean requestAll();

}
