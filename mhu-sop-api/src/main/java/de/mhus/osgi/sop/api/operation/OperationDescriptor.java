package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.basics.Named;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MNlsProvider;
import de.mhus.lib.core.util.Nls;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.lib.core.util.Version;

public class OperationDescriptor implements MNlsProvider, Nls, Named, Versioned {

	private Collection<String> tags;
	private String path;
	private Version version;
	private String source;
	private ParameterDefinitions definitions;
	private DefRoot form;
	private MNls nls;
	private MNlsProvider nlsProvider;
	private String title;
	private Operation operation;

	public OperationDescriptor(
			Operation operation, 
			Collection<String> tags, 
			String source, 
			ParameterDefinitions pDef, 
			DefRoot form, 
			MNlsProvider nls, 
			String title
		) {
		this.operation = operation;
		this.tags = tags;
		this.source = source;
		this.path = operation.getDescription().getPath();
		this.version = operation.getDescription().getVersion();
		this.definitions = operation.getDescription().getParameterDefinitions();
		this.form = form;
		this.nlsProvider = operation;
		this.title = title;
		
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public boolean canExecute(Collection<String> providedTags, IProperties properties) {
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
		DefaultTaskContext context = new DefaultTaskContext(operation.getClass());
		context.setParameters(properties);
		return operation.canExecute(context);
	}

	public Collection<String> getTags() {
		return tags;
	}

	@Override
	public String getName() {
		return path;
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public String getVersionString() {
		return version.toString();
	}
	public Version getVersion() {
		return version;
	}
	
	public String getSource() {
		return source;
	}

	/**
	 * Every action should have a parameter definition. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public ParameterDefinitions getParameterDefinitions() {
		return definitions;
	}
	
	/**
	 * An action can provide a form component but it's not necessary. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public DefRoot getForm() {
		return form;
	}

	@Override
	public String nls(String text) {
		return MNls.find(this, text);
	}

	@Override
	public MNls getNls() {
		if (nls == null)
			nls = nlsProvider.getNls();
		return nls;
	}

	public String getCaption() {
		return nls("caption=" + getTitle());
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, path, version, tags);
	}


}
