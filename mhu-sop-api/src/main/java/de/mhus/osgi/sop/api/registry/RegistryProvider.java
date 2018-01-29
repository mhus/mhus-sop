package de.mhus.osgi.sop.api.registry;

public interface RegistryProvider {

	void publish(RegistryValue entry);

	void remove(String path);

	void publishAll();

	void requestAll();

}
