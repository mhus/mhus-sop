/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.impl.registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.core.directory.WritableResourceNode;
import de.mhus.lib.core.service.ServerIdent;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.errors.UsageException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.lib.karaf.MServiceTracker;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryPathControl;
import de.mhus.osgi.sop.api.registry.RegistryProvider;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(provide={RegistryApi.class,RegistryManager.class},immediate=true)
public class RegistryApiImpl extends MLog implements RegistryApi, RegistryManager, CfgProvider {

	public static final int DEFAULT_PRIORITY = 100;
	private IConfig configProxy = new MyConfig();
	private TreeMap<String, RegistryValue> registry = new TreeMap<>();
	private TimerIfc timer;
	private MTimerTask timerTask;
	private CfgLong CFG_UPDATE_INTERVAL = new CfgLong(RegistryApiImpl.class, "updateInterval", 60000);
	private String ident;
	private TreeSet<ControlDescriptor> pathControllers = new TreeSet<>();
	private MServiceTracker<RegistryPathControl> pathControllerTracker = new MServiceTracker<RegistryPathControl>(RegistryPathControl.class) {
		
		@Override
		protected void removeService(ServiceReference<RegistryPathControl> reference, RegistryPathControl service) {
			String path = (String) reference.getProperty("path");
			if (path == null) return;
			synchronized (pathControllers) {
				pathControllers.remove(new ControlDescriptor(path));
			}
		}
		
		@Override
		protected void addService(ServiceReference<RegistryPathControl> reference, RegistryPathControl service) {
			String path = (String) reference.getProperty("path");
			if (path == null) {
				log().w("Found PathControl without path",reference,service);
				return;
			}
			if (path.indexOf('@') > 0)
				path = validateParameterPath(path);
			else
				path = validateNodePath(path);
			synchronized (pathControllers) {
				pathControllers.add(new ControlDescriptor(reference, service));
			}
		}
	};

	@Activate
	public void doActivate(ComponentContext ctx) {
		MApi.get().getCfgManager().registerCfgProvider(RegistryApi.class.getCanonicalName(), this);
		ident = MApi.lookup(ServerIdent.class).toString();
		pathControllerTracker.start();
		load(false);
		MThread.asynchron(new Runnable() {
			
			@Override
			public void run() {
				MThread.sleep(10000);
				while (!publishAll()) {
					MThread.sleep(10000);
				}
			}
		});
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (timer != null)
			timer.cancel();
		pathControllerTracker.stop();
	}

	@Reference(service=TimerFactory.class)
	public void setTimerFactory(TimerFactory factory) {
//		log().i("create timer");
		timer = factory.getTimer();
		timerTask = new MTimerTask() {
			
			@Override
			public void doit() throws Exception {
				checkUpdate();
			}
		};
		timer.schedule(timerTask, 10000, CFG_UPDATE_INTERVAL.value() );
	}

	protected void checkUpdate() {
		
		// send worker update
		setParameter(PATH_WORKER + ident + "@pid", MSystem.getHostname() + ":" + MSystem.getPid(), CFG_UPDATE_INTERVAL.value() * 2, true, false, false);
		
		final long now = System.currentTimeMillis();
		LinkedList<RegistryValue> values = null;
		synchronized (registry) {
			values = new LinkedList<>(registry.values());
		}
		
		// remove all out timed entries
		HashSet<String> lostWorkers = new HashSet<>();
		for (RegistryValue value : values) {
			boolean remove =  !value.isLocal() && value.getTimeout() > 0 && now - value.getUpdated() > value.getTimeout();
			if (remove) {
				String path = value.getPath();
				if (path.startsWith(PATH_WORKER) && path.endsWith("@pid") && path.indexOf('/', PATH_WORKER.length()+1) < 0)
					lostWorkers.add(path.substring(PATH_WORKER.length(), path.length()-4 ));
				removeLocalParameter(path, value.getSource(), true);
			}
		}
		if (lostWorkers.size() > 0) {
			for (RegistryValue value : values) {
				if (value.isLocal()) {
					RegistryValue remote = value.getRemoteValue();
					if (remote != null && lostWorkers.contains(remote.getSource()) ) {
						removeLocalParameter(value.getPath(), remote.getSource(), true);
					}
				} else
				if (lostWorkers.contains(value.getSource())) {
					removeLocalParameter(value.getPath(), value.getSource(), true);
				}
			}
		}
	}

	@Override
	public RegistryValue getParameter(String path) {
		path = validateParameterPath(path);
		synchronized (registry) {
			return registry.get(path);
		}
	}

	@Override
	public Set<String> getNodeChildren(String path) {
		path = validateNodePath(path);
		final TreeSet<String> out = new java.util.TreeSet<>();
		final String pathx = path.equals("/") ? path : path + "/";
		final int posx = pathx.length()+1;
		synchronized (registry) {
			for (Entry<String, RegistryValue> entry : registry.entrySet()) {
				if (entry.getKey().startsWith(pathx)) {
					String k = entry.getKey();
					int p = k.indexOf('@');
					if (p > 0) k = k.substring(0, p);
					p = k.indexOf('/', posx);
					if (p > 0)
						out.add(k.substring(posx-1,p));
					else
						out.add(k.substring(posx-1));
				} else 
				if (out.size() > 0 || entry.getKey().compareTo(pathx) > 0) break; // in ordered TreeHash we found one and now it's the end of the block
			}
		}
		return out;
	}

	@Override
	public Set<String> getParameterNames(String path) {
		path = validateNodePath(path);
		final TreeSet<String> out = new java.util.TreeSet<>();
		final String pathx = path + "@";
		final int posx = pathx.length()+1;
		synchronized (registry) {
			for (Entry<String, RegistryValue> entry : registry.entrySet()) {
				if (entry.getKey().startsWith(pathx)) {
					out.add(entry.getKey().substring(posx));
				} else
				if (out.size() > 0 || entry.getKey().compareTo(pathx) > 0) break; // in ordered TreeHash we found one and now it's the end of the block
			}
		}
		return out;
	}

	@Override
	public Set<RegistryValue> getParameters(String path) {
		path = validateNodePath(path);
		final TreeSet<RegistryValue> out = new java.util.TreeSet<>();
		final String pathx = path + "@";
		synchronized (registry) {
			for (Entry<String, RegistryValue> entry : registry.entrySet()) {
				if (entry.getKey().startsWith(pathx)) {
					out.add(entry.getValue());
				} else
				if (out.size() > 0 || entry.getKey().compareTo(pathx) > 0) break; // in ordered TreeHash we found one and now it's the end of the block
			}
		}
		return out;
	}

	@Override
	public boolean setParameter(String path, String value, long timeout, boolean readOnly, boolean persistent, boolean local) {
		path = validateParameterPath(path);
		if (value == null) throw new NullPointerException("null value not allowed");
		String source = ident;
		if (local) {
			source = SOURCE_LOCAL;
			timeout = 0;
		}
		RegistryValue current = getParameter(path);
		
		RegistryValue entry = new RegistryValue(value, source, System.currentTimeMillis(), path, timeout, readOnly, persistent);
		if (current != null) {
			RegistryValue c = current;
			if (!local && current.isLocal()) c = current.getRemoteValue();
			if (!local && c != null) {
				if (	c.getTimeout() == 0
						&& timeout == 0
						&& MSystem.equals(c.getValue(), value) 
						&& c.isReadOnly() == readOnly 
						&& c.isPersistent() == persistent
					) return false;
				if (c.isReadOnly() && !c.getSource().equals(source))
					throw new AccessDeniedException("The entry is read only");
			}
			if (!local && current.isLocal()) {
				current.setRemoteValue(entry);
				return false;
			}
			if (entry.isLocal()) {
				entry.setRemoteValue(c);
			}
		}
		
		// let the controller check the action
		if (!path.startsWith(PATH_WORKER) && !entry.isLocal()) {
			RegistryPathControl controller = getPathController(path);
			if (controller != null) {
				entry = controller.checkSetParameter(this, entry);
				if (entry == null) return false;
				if (!path.equals(entry.getPath())) throw new NotSupportedException("Controller can't change the path of the entry",path);
			}
		}

		// Put into registry
		synchronized (registry) {
			registry.put(path, entry);
		}
				
		// save to file if/was persistent
		if (entry.isPersistent() || current != null && current.isPersistent())
			try {
				save();
			} catch (IOException e) {
				log().d(e);
			}
		// publish to other nodes
		if (!path.startsWith(RegistryApi.PATH_LOCAL) && !entry.isLocal()) {
			for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
				try {
					provider.publish(entry);
				} catch (Throwable t) {
					log().d(provider,t);
				}
			}
		}
		// fire Cfg events
		if (!path.startsWith(RegistryApi.PATH_SYSTEM) && !path.startsWith(RegistryApi.PATH_WORKER)) {
			try {
				MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
			} catch (Throwable t) {
				log().d(t);
			}
		}
		return true;
	}

	@Override
	public RegistryPathControl getPathController(String path) {
		synchronized (pathControllers) {
			// pathControllers is ordered by priority and reverse path, use the first one matching
			for (ControlDescriptor desc : pathControllers)
				if (path.startsWith(desc.path)) {
					if (desc.service.isTakeControl(path))
						return desc.service;
				}
		}
		return null;
	}

	@Override
	public boolean removeParameter(String path) {
		path = validateParameterPath(path);
		RegistryValue current = getParameter(path);
		if (current == null) return false;
		
		// in case of local ...
		if (current.isLocal()) {
			RegistryValue entry = current.getRemoteValue();
			synchronized (registry) {
				registry.remove(path);
			}
			if (entry != null) {
				setParameterFromRemote(entry);
				if (entry != null && entry.isPersistent())
					try {
						save();
					} catch (IOException e) {
						log().d(e);
					}
				return true;
			} else {
				// send Cfg events
				if (!path.startsWith(RegistryApi.PATH_SYSTEM)) {
					try {
						MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
					} catch (Throwable t) {
						log().d(t);
					}
				}
				return true;
			}
		}
		
		if (current.isReadOnly() && !current.getSource().equals(ident))
			throw new AccessDeniedException("The entry is readOnly");
	
		// let the controller check the action
		if (!path.startsWith(PATH_WORKER) && !current.isLocal()) {
			RegistryPathControl controller = getPathController(path);
			if (controller != null) {
				if (!controller.checkRemoveParameter(this, current)) return false;
			}
		}

		// update memory registry
		RegistryValue entry = null;
		synchronized (registry) {
			entry = registry.remove(path);
		}
		// save to disk if was persistent
		if (entry != null && entry.isPersistent())
			try {
				save();
			} catch (IOException e) {
				log().d(e);
			}
		// publish to other nodes
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				provider.remove(path);
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
		// send Cfg events
		if (!path.startsWith(RegistryApi.PATH_SYSTEM) && !path.startsWith(RegistryApi.PATH_WORKER)) {
			try {
				MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
			} catch (Throwable t) {
				log().d(t);
			}
		}
		return true;
	}

	@Override
	public void setParameterFromRemote(RegistryValue value) {
		if (value == null || value.getPath() == null || value.getValue() == null) throw new NullPointerException();
		if (value.getPath().startsWith(RegistryApi.PATH_LOCAL) || value.isLocal()) return;
		
		// let the controller check the action
		if (!value.getPath().startsWith(PATH_WORKER) && !value.isLocal()) {
			RegistryPathControl controller = getPathController(value.getPath());
			if (controller != null) {
				value = controller.checkSetParameterFromRemote(this, value);
				if (value == null) return;
			}
		}

		synchronized (registry) {
			RegistryValue cur = registry.get(value.getPath());
			if (cur != null ) {
				if (cur.isLocal()) {
					cur.setRemoteValue(value);
					return;
				}
				if ( cur.isReadOnly() && !cur.getSource().equals(value.getSource()))
					return;
			}
			registry.put(value.getPath(), value);
		}
		if (!value.getPath().startsWith(RegistryApi.PATH_SYSTEM) && !value.getPath().startsWith(RegistryApi.PATH_WORKER)) {
			try {
				MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), value.getPath());
			} catch (Throwable t) {
				log().d(t);
			}
		}
	}

	@Override
	public void removeParameterFromRemote(String path, String source) {
		removeLocalParameter(path, source, false);
	}
	
	public void removeLocalParameter(String path, String source, boolean intern) {
		if (path == null) return;
		if (intern && path.startsWith(RegistryApi.PATH_LOCAL)) return;

		RegistryValue cur = null;
		synchronized (registry) {
			cur = registry.get(path);
		}
		if (cur == null) return; // path not exists
		
		// let the controller check the action
		if (!path.startsWith(PATH_WORKER) && !cur.isLocal()) {
			RegistryPathControl controller = getPathController(path);
			if (controller != null) {
				if (!controller.checkRemoveParameterFromRemote(this, cur))
					return;
			}
		}

		synchronized (registry) {
			if (cur.isLocal()) {
				RegistryValue c = cur.getRemoteValue();
				if (c != null && !intern && c.isReadOnly() && !c.getSource().equals(source))
					return;
				cur.setRemoteValue(null);
				return;
			}
			if (source != null) {
				if (cur != null && !intern && cur.isReadOnly() && !cur.getSource().equals(source))
					return;
			}
			registry.remove(path);
		}
		if (!path.startsWith(RegistryApi.PATH_SYSTEM) && !path.startsWith(RegistryApi.PATH_WORKER)) {
			try {
				MApi.getCfgUpdater().doUpdate(RegistryApi.class.getCanonicalName(), path);
			} catch (Throwable t) {
				log().d(t);
			}
		}
	}

	@Override
	public Collection<RegistryValue> getAll() {
		synchronized (registry) {
			return Collections.unmodifiableCollection(new LinkedList<>(registry.values()));
		}
	}

	@Override
	public boolean publishAll() {
		boolean res = true;
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				if (!provider.publishAll()) res = false;
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
		return res;
	}

	@Override
	public boolean requestAll() {
		boolean res = true;
		for (RegistryProvider provider : MOsgi.getServices(RegistryProvider.class, null)) {
			try {
				if (!provider.requestAll()) res = false;
			} catch (Throwable t) {
				log().d(provider,t);
			}
		}
		return res;
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

		@Override
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
			RegistryValue value = getParameter(name);
			if (value == null) return null;
			return value.getValue();
		}

		@Override
		public boolean isProperty(String name) {
			RegistryValue value = getParameter(name);
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

		@Override
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
			RegistryValue value = getParameter(path + "@" + name);
			if (value == null) return null;
			return value.getValue();
		}

		@Override
		public boolean isProperty(String name) {
			RegistryValue value = getParameter(path + "@" + name);
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

	@Override
	public void save() throws IOException {
		MProperties p = new MProperties();
		for (RegistryValue entry : getAll())
			if ((entry.getSource().equals(ident) || entry.isLocal()) && entry.isPersistent()) {
				p.setString(entry.getPath(), entry.isReadOnly() + "|" + entry.getTimeout() + "|" + entry.getSource() + "|" + entry.getValue());
			}
		p.save(getFile());
	}

	@Override
	public void load() {
		load(true);
	}
	
	public void load(boolean push) {
		File file = getFile();
		if (!file.exists()) return;
		MProperties prop = MProperties.load(file);
		long updated = System.currentTimeMillis();
		for (Entry<String, Object> entry : prop.entrySet()) {
			String[] split = entry.getValue().toString().split("\\|", 4);
			if (!SOURCE_LOCAL.equals(split[2])) {
				split[2] = ident;
				setParameterFromRemote(new RegistryValue(split[3], split[2], updated, entry.getKey(), MCast.tolong(split[1],0), MCast.toboolean(split[0], true), true));
			} else {
				setParameter(entry.getKey(), split[3], 0, true, true, true);
			}
		}
		if (push)
			publishAll();
	}
	
	private File getFile() {
		return new File("etc/" + RegistryApi.class.getCanonicalName() + ".properties");
	}

	private class ControlDescriptor implements Comparable<ControlDescriptor> {

		private String orgPath;
		private String path;
		private int priority;
		private long bundleId;
		private RegistryPathControl service;

		public ControlDescriptor(String path) {
			orgPath = path;
		}
		
		public ControlDescriptor(ServiceReference<RegistryPathControl> reference, RegistryPathControl service) {
			orgPath = (String) reference.getProperty("path");
			path = orgPath;
			if (!path.endsWith("/") && !path.endsWith("@")) path = path +"/";
			priority = MCast.toint(reference.getProperty("priority"), DEFAULT_PRIORITY);
			bundleId = reference.getBundle().getBundleId();
			this.service = service;
		}

		@Override
		public int compareTo(ControlDescriptor o) {
			// order priority to top
			int out = 0;
			out = Integer.compare(priority, o.priority);
			if (out != 0) return out;
			// order path reverse - longer path first
			out = -path.compareTo(o.path);
			if (out != 0) return out;
			// if path and prio are the same, compare the service object, allow multiple services with the same path.
			out = Integer.compare(service.hashCode(), o.service.hashCode());
			return out;
		}
		
		@Override
		public boolean equals(Object in) {
			if (in == null) return false;
			if (in instanceof String)
				return orgPath.equals(in);
			if (in instanceof ControlDescriptor) {
				ControlDescriptor d = (ControlDescriptor)in;
				return service == d.service;
				// return orgPath.equals(d.orgPath) && priority == d.priority && hash == d.hash;
			}
			if (in instanceof RegistryPathControl)
				return service == in;
			return false;
		}
		
		@Override
		public String toString() {
			return path + "(" + priority + ") [" + bundleId + "]";
		}
		
	}
}
