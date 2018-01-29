package de.mhus.osgi.sop.api.registry;

public class RegistryValue {

	private String value;
	private String source;
	private long updated;
	private String path;
	
	public RegistryValue(String value, String source, long updated, String path) {
		super();
		this.value = value;
		this.source = source;
		this.updated = updated;
		this.path = path;
	}

	public String getValue() {
		return value;
	}

	public String getSource() {
		return source;
	}

	public long getUpdated() {
		return updated;
	}

	public String getPath() {
		return path;
	}
	
}
