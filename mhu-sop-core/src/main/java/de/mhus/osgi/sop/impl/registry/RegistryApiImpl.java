package de.mhus.osgi.sop.impl.registry;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.core.directory.WritableResourceNode;
import de.mhus.lib.core.service.ServerIdent;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.UsageException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryProvider;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(provide={RegistryApi.class,RegistryManager.class})
public class RegistryApiImpl extends MLog implements RegistryApi, RegistryManager, CfgProvider {

	private IConfig configProxy = new MyConfig();
	private HashMap<String, RegistryValue> registry = new HashMap<>();

	@Activate
	public void doActivate(ComponentContext ctx) {
		MApi.get().getCfgManager().registerCfgProvider(RegistryApi.class.getCanonicalName(), this);
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {

	}

	@Override
	public RegistryValue getNodeParameter(String path) {
		path = validateParameterPath(path);
		synchronized (registry) {
			return registry.get(path);
		}
	}

	@Override
	public Set<String> getNodeChildren(String path) {
		path = validateNodePath(path);
		TreeSet<String> out = new java.util.TreeSet<>();
		String pathx = path + "/";
		int posx = pathx.length()+1;
		synchronized (registry) {
			registry.forEach((k,v) -> { 
				if (k.startsWith(pathx)) {
					int p = k.indexOf('@');
					if (p > 0) k = k.substring(0, p);
					p = k.indexOf('/', posx);
					if (p > 0)
						out.add(k.substring(p));
					else
						out.add(k);
				}
			});
		}
		return out;
	}

	@Override
	public Set<String> getParameterNames(String path) {
		path = validateNodePath(path);
		TreeSet<String> out = new java.util.TreeSet<>();
		String pathx = path + "@";
		int posx = pathx.length()+1;
		synchronized (registry) {
			registry.forEach((k,v) -> { 
				if (k.startsWith(pathx)) {
					out.add(k.substring(posx));
				}
			});
		}
		return out;
	}

	@Override
	public Set<RegistryValue> getParameters(String path) {
		path = validateNodePath(path);
		TreeSet<RegistryValue> out = new java.util.TreeSet<>();
		String pathx = path + "@";
		synchronized (registry) {
			registry.forEach((k,v) -> { 
				if (k.startsWith(pathx)) {
					out.add(v);
				}
			});
		}
		return out;
	}

	@Override
	public boolean setParameter(String path, String value) {
		path = validateParameterPath(path);
		if (value == null) throw new NullPointerException("null value not allowed");
		RegistryValue current = getNodeParameter(path);
		String source = MApi.lookup(ServerIdent.class).toString();
		
		if (current != null) {
			if (MSystem.equals(current.getValue(), value)) return false;
		}
		RegistryValue entry = new RegistryValue(value, source, System.currentTimeMillis(), path);
		synchronized (registry) {
			registry.put(path, entry);
		}
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				provider.publish(entry);
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
		try {
			MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
		} catch (Throwable t) {
			log().d(t);
		}
		
		return true;
	}

	@Override
	public boolean removeParameter(String path) {
		path = validateParameterPath(path);
		RegistryValue current = getNodeParameter(path);
		if (current == null) return false;
		synchronized (registry) {
			registry.remove(path);
		}
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				provider.remove(path);
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
		
		try {
			MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
		} catch (Throwable t) {
			log().d(t);
		}

		return true;
	}

	@Override
	public void setLocalParameter(RegistryValue value) {
		if (value == null || value.getPath() == null || value.getValue() == null) throw new NullPointerException();
		synchronized (registry) {
			registry.put(value.getPath(), value);
		}
		
		try {
			MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), value.getPath());
		} catch (Throwable t) {
			log().d(t);
		}

	}

	@Override
	public void removeLocalParameter(String path, String source) {
		synchronized (registry) {
			if (source != null) {
				RegistryValue value = registry.get(path);
				if (value == null) return;
				if (!source.equals(value.getSource())) return;
			}
			registry.remove(path);
		}
		
		try {
			MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
		} catch (Throwable t) {
			log().d(t);
		}

	}

	@Override
	public Collection<RegistryValue> getAll() {
		synchronized (registry) {
			return Collections.unmodifiableCollection(registry.values());
		}
	}

	@Override
	public void publishAll() {
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				provider.publishAll();
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
	}

	@Override
	public void requestAll() {
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				provider.requestAll();
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
	}
	
	private String validateParameterPath(String name) {
		if (name == null) throw new NullPointerException();
		if (!name.startsWith("/")) name = "/" + name;

		if (name.indexOf('\\') >= 0) name = name.replaceAll("\\\\", "/");
		if (name.indexOf('*') >= 0) name = name.replaceAll("\\*", "_");
		if (name.indexOf('?') >= 0) name = name.replaceAll("\\?", "_");
		if (name.indexOf(':') >= 0) name = name.replaceAll(":", "_");
		if (name.indexOf(' ') >= 0) name = name.replaceAll(" ", "_");
		if (name.indexOf("..") >= 0) name = name.replaceAll("..", "_");
		if (name.indexOf('~') >= 0) name = name.replace('~', '_');
		while (name.indexOf("//") >= 0) name = name.replace("//", "/");
		
		if (name.indexOf('@') < 0) throw new UsageException("Parameter is not defined in path");

		return name;
	}

	private String validateNodePath(String name) {
		if (name == null) throw new NullPointerException();
		if (!name.startsWith("/")) name = "/" + name;

		if (name.indexOf('\\') >= 0) name = name.replaceAll("\\\\", "/");
		if (name.indexOf('*') >= 0) name = name.replaceAll("\\*", "_");
		if (name.indexOf('?') >= 0) name = name.replaceAll("\\?", "_");
		if (name.indexOf(':') >= 0) name = name.replaceAll(":", "_");
		if (name.indexOf(' ') >= 0) name = name.replaceAll(" ", "_");
		if (name.indexOf("..") >= 0) name = name.replaceAll("..", "_");
		if (name.indexOf('~') >= 0) name = name.replace('~', '_');
		while (name.indexOf("//") >= 0) name = name.replace("//", "/");
		
		if (name.indexOf('@') >= 0) throw new UsageException("Parameter can't be defined in node path");

		return name;
	}

	@Override
	public IConfig getConfig() {
		return configProxy;
	}

	@Override
	public void doStart(String name) {
		
	}

	@Override
	public void doStop() {
		
	}

	private class MyConfig extends IConfig {

		private static final long serialVersionUID = 1L;

		public IConfig getNodeByPath(String path) {
			return new MySubConfig(path);
		}

		@Override
		public void clear() {
		}

		@Override
		public WritableResourceNode<IConfig> createConfig(String key) throws MException {
			return null;
		}

		@Override
		public int moveConfig(IConfig config, int newPos) throws MException {
			return 0;
		}

		@Override
		public void removeConfig(IConfig config) throws MException {
		}

		@Override
		public Collection<String> getPropertyKeys() {
			return null;
		}

		@Override
		public IConfig getNode(String key) {
			return null;
		}

		@Override
		public Collection<IConfig> getNodes() {
			return null;
		}

		@Override
		public Collection<IConfig> getNodes(String key) {
			return null;
		}

		@Override
		public Collection<String> getNodeKeys() {
			return null;
		}

		@Override
		public String getName() throws MException {
			return null;
		}

		@Override
		public InputStream getInputStream(String rendition) {
			return null;
		}

		@Override
		public ResourceNode<?> getParent() {
			return null;
		}

		@Override
		public URL getUrl() {
			return null;
		}

		@Override
		public Object getProperty(String name) {
			RegistryValue value = getNodeParameter(name);
			if (value == null) return null;
			return value.getValue();
		}

		@Override
		public boolean isProperty(String name) {
			RegistryValue value = getNodeParameter(name);
			return value != null;
		}

		@Override
		public void removeProperty(String key) {
		}

		@Override
		public void setProperty(String key, Object value) {
		}

		@Override
		public boolean isEditable() {
			return false;
		}
		
	}
	
	private class MySubConfig extends IConfig {

		private static final long serialVersionUID = 1L;
		private String path;

		public MySubConfig(String path) {
			this.path = path;
		}

		public IConfig getNodeByPath(String path) {
			return new MySubConfig(this.path + path);
		}

		@Override
		public void clear() {
		}

		@Override
		public WritableResourceNode<IConfig> createConfig(String key) throws MException {
			return null;
		}

		@Override
		public int moveConfig(IConfig config, int newPos) throws MException {
			return 0;
		}

		@Override
		public void removeConfig(IConfig config) throws MException {
		}

		@Override
		public Collection<String> getPropertyKeys() {
			return null;
		}

		@Override
		public IConfig getNode(String key) {
			return null;
		}

		@Override
		public Collection<IConfig> getNodes() {
			return null;
		}

		@Override
		public Collection<IConfig> getNodes(String key) {
			return null;
		}

		@Override
		public Collection<String> getNodeKeys() {
			return null;
		}

		@Override
		public String getName() throws MException {
			return null;
		}

		@Override
		public InputStream getInputStream(String rendition) {
			return null;
		}

		@Override
		public ResourceNode<?> getParent() {
			return null;
		}

		@Override
		public URL getUrl() {
			return null;
		}

		@Override
		public Object getProperty(String name) {
			RegistryValue value = getNodeParameter(path + "@" + name);
			if (value == null) return null;
			return value.getValue();
		}

		@Override
		public boolean isProperty(String name) {
			RegistryValue value = getNodeParameter(path + "@" + name);
			return value != null;
		}

		@Override
		public void removeProperty(String key) {
		}

		@Override
		public void setProperty(String key, Object value) {
		}

		@Override
		public boolean isEditable() {
			return false;
		}
		
	}
}
