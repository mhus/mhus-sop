package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.basics.Named;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MNlsProvider;
import de.mhus.lib.core.util.Nls;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.lib.core.util.Version;

public class OperationDescriptor implements MNlsProvider, Nls, Named, Versioned {

	private Collection<String> tags;
	private OperationAddress address;
	private OperationDescription description;

	public OperationDescriptor(
			String address,
			OperationDescription description,
			Collection<String> tags
		) {
		this(new OperationAddress(address), description, tags);
		
	}
	
	public OperationDescriptor(OperationAddress address, OperationDescription description,
			Collection<String> tags) {
		this.address = address;
		this.description = description;
		this.tags = tags;
	}

	public boolean compareTags(Collection<String> providedTags) {
		if (providedTags == null) return false;
		// negative check
		for (String t : tags) {
			if (t.startsWith("*")) {
				if (!providedTags.contains(t.substring(1)))
					return false;
			} else
			if (t.startsWith("!")) {
				if (providedTags.contains(t.substring(1)))
					return false;
			}
		}
		// positive check
		for (String t : providedTags) {
			if (t.startsWith("!")) {
				if (tags.contains(t.substring(1)))
					return false;
			} else
			if (!tags.contains(t)) 
				return false;
		}
		return true;
	}
	
	public Collection<String> getTags() {
		return tags;
	}
		
	/**
	 * Every action should have a parameter definition. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public ParameterDefinitions getParameterDefinitions() {
		return description.getParameterDefinitions();
	}
	
	/**
	 * An action can provide a form component but it's not necessary. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public DefRoot getForm() {
		return description.getForm();
	}

	@Override
	public String nls(String text) {
		return description.nls(text);
	}

	@Override
	public MNls getNls() {
		return description.getNls();
	}

	public String getCaption() {
		return description.getCaption();
	}

	public String getTitle() {
		return description.getTitle();
	}
	
	@Override
	public String getName() {
		return address.getName();
	}
	
	public String getPath() {
		return address.getPath();
	}
	
	public Version getVersion() {
		return address.getVersion();
	}
	
	public OperationAddress getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, address, tags);
	}

	@Override
	public String getVersionString() {
		return address.getVersionString();
	}

	public String getProvider() {
		return address.getProvider();
	}
	
}
