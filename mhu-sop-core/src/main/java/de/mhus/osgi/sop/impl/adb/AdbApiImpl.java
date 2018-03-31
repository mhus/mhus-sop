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
package de.mhus.osgi.sop.impl.adb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.ContextCachedItem;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.SopActionTask;
import de.mhus.osgi.sop.api.model.SopObjectParameter;
import de.mhus.osgi.sop.impl.AaaContextImpl;

//@Component(immediate=true) done by blueprint
public class AdbApiImpl extends MLog implements AdbApi {

	private HashMap<String, DbSchemaService> controllers = new HashMap<String, DbSchemaService>();
	private ServiceTracker<DbSchemaService,DbSchemaService> accessTracker;
	private BundleContext context;

//	@Activate
	public void doActivate() {
		BundleContext context = FrameworkUtil.getBundle(AdbApiImpl.class).getBundleContext();
		doActivate(context);
	}
	public void doActivate(BundleContext context) {
		this.context = context;
		init();
	}
	
	private void init() {
		if (accessTracker == null) {
			if (context == null)
				context = FrameworkUtil.getBundle(AdbApiImpl.class).getBundleContext();
			if (context != null) {
				accessTracker = new ServiceTracker<>(context, DbSchemaService.class, new MyAccessTrackerCustomizer() );
				accessTracker.open();
			}
		}
	}
//	@Deactivate
	public void doDeactivate() {
		if (accessTracker != null)
			accessTracker.close();
		accessTracker = null;
		context = null;
		if (controllers != null)
			controllers.clear();
	}

	@Override
	public SopActionTask createActionTask(String queue, String action, String target, String[] properties, boolean smart) throws MException {

		XdbService manager = getManager();	

		if (smart) {
			SopActionTask t = manager.getObjectByQualification(Db.query(SopActionTask.class).eq("queue", queue).eq("action", action).eq("target", target));
			if (t != null) return t;
		}		
		
		SopActionTask task = manager.inject(new SopActionTask());
		task.setQueue(queue);
		task.setAction(action);
		task.setProperties(properties);
		task.setTarget(target);
		
		task.save();
		
		return task;
	}

	@Override
	public List<SopActionTask> getQueue(String queue, int max) throws MException {
		LinkedList<SopActionTask> out = new LinkedList<SopActionTask>();
		XdbService manager = getManager();	
		
		DbCollection<SopActionTask> res = manager.getByQualification(Db.query(SopActionTask.class).eq(Db.attr("queue"), Db.value(queue)));
		for (SopActionTask task : res) {
			out.add(task);
			if (max > 0 && out.size() >= max) break;
		}
		res.close();
		return out;
	}

	@Override
	public List<SopObjectParameter> getParameters(Class<?> type, UUID id) throws MException {
		return getParameters(type.getCanonicalName(), id);
	}
	
	@Override
	public List<SopObjectParameter> getParameters(String type, UUID id) throws MException {
		
		
		XdbService manager = getManager();
		List<SopObjectParameter> out = manager.getByQualification(
				Db.query(SopObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("objectid"),Db.value(id))
				).toCacheAndClose();
		
		return out;
	}

	@Override
	public void setGlobalParameter(String key, String value) throws MException {
		setParameter(SopObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key, value);
	}

	@Override
	public void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		setParameter(type.getCanonicalName(), id, key, value);
	}
	
	@Override
	public void setParameter(String type, UUID id, String key, String value) throws MException {
				
		SopObjectParameter out = getParameter(type, id, key);
		if (out == null) {
			XdbService manager = getManager();
			if (key == null) return;
			out = manager.inject(new SopObjectParameter());
			out.setObjectType(type);
			out.setObjectId(id);
			out.setKey(key);
		} else if (value == null) {
			out.delete();
			return;
		}
		out.setValue(value);
		out.save();
	}
	
//	public ObjectParameter getRecursiveParameter(String type, UUID id, String key) throws MException {
//		
//		ObjectParameter out = null;
//		while (out == null && type != null) {
//			out = getParameter(type, id, key);
//			if (out == null) {
//				id = getParentId(type,id);
//				type = getTypeParent(type);
//			}
//		}
//		
//		return out;
//	}
	
//	public String getRecursiveValue(String type, UUID id, String key, String def) {
//		try {
//			ObjectParameter out = getRecursiveParameter(type, id, key);
//			return out == null || out.getValue() == null ? def : out.getValue();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//		return def;
//	}
	
	@Override
	public SopObjectParameter getGlobalParameter(String key) throws MException {
		return getParameter(SopObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key);
	}
	
	@Override
	public String getValue(Class<?> type, UUID id, String key, String def) throws MException {
		return getValue(type.getCanonicalName(), id, key, def);
	}
	
	@Override
	public String getValue(String type, UUID id, String key, String def) throws MException {
		SopObjectParameter p = getParameter(type, id, key);
		if (p == null || p.getValue() == null) return def;
		return p.getValue();
	}
	
	@Override
	public SopObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return getParameter(type.getCanonicalName(), id, key);
	}
	
	@Override
	public SopObjectParameter getParameter(String type, UUID id, String key) throws MException {
		
		XdbService manager = getManager();
		SopObjectParameter out = manager.getByQualification(
				Db.query(SopObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("objectid"),Db.value(id))
				.eq(Db.attr("key"), Db.value(key))
				).getNextAndClose();
		return out;
	}

	@Override
	public SopObjectParameter getRecursiveParameter(DbMetadata obj, String key) throws MException {
		int level = 10;
		while (obj != null && level > 0) {
			if (obj == null || key == null) return null;
			SopObjectParameter out = getParameter(obj.getClass().getCanonicalName(), obj.getId(), key);
			if (out != null) return out;
			obj = obj.findParentObject();
			level--;
		}
		return null;
	}

	public List<UUID> getIds(String type, String key, String value) throws MException {

		LinkedList<UUID> out = new LinkedList<>();
		XdbService manager = getManager();
		for ( SopObjectParameter p : manager.getByQualification(
				Db.query(SopObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("key"),Db.value(key))
				.eq(Db.attr("value"), Db.value(value))
				)) {
				out.add(p.getObjectId());
		}
		return out;
	}

	public void deleteAll(String type, UUID id) throws MException {

		for (SopObjectParameter p : getParameters( type, id))
			if (canDelete(p))
				p.delete();
	}

	@Override
	public XdbService getManager() {
		init();
		return SopDbImpl.getManager();
	}

	@Override
	public void deleteParameters(Class<?> type, UUID id) throws MException {
		for (SopObjectParameter p : getParameters( type, id))
			p.delete();
	}

	@Override
	public List<SopObjectParameter> getParameters(Class<?> type, String key,
			String value) throws MException {
		LinkedList<SopObjectParameter> out = new LinkedList<>();
		XdbService manager = SopDbImpl.getManager();
		for ( SopObjectParameter p : manager.getByQualification(
				Db.query(SopObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type.getCanonicalName()))
				.eq(Db.attr("key"),Db.value(key))
				.eq(Db.attr("value"), Db.value(value))
				)) {
			out.add(p);
		}
		return out;
	}

	@Override
	public <T> LinkedList<T> collectResults(AQuery<T> query, int page) throws MException {
		LinkedList<T> list = new LinkedList<T>();
		DbCollection<T> res = getManager().getByQualification(query);
		if (!res.skip(page * PAGE_SIZE)) return list;
		while (res.hasNext()) {
			list.add(res.next());
			if (list.size() >= PAGE_SIZE) break;
		}
		res.close();
		return list;
	}

	@Override
	public List<SopActionTask> getActionTaskPage(String queue, int size) {
		
		LinkedList<SopActionTask> out = new LinkedList<SopActionTask>();
		try {
			DbCollection<SopActionTask> res = getManager().getByQualification(Db.query(SopActionTask.class).eq("queue", queue).desc("creationdate"));
			while (res.hasNext()) {
				out.add(res.next());
				if (out.size() >= size) break;
			}
			res.close();
		} catch (Throwable t) {
			log().e(queue,t);
		}
		return out;
	}


	protected DbSchemaService getController(String type) throws MException {
		init();
		if (type == null) throw new MException("type is null");
		DbSchemaService ret = controllers.get(type);
		if (ret == null) throw new MException("Access Controller not found",type);
		return ret;
	}

	protected boolean canRead(AaaContext c, DbMetadata obj)
			throws MException {
		
		Boolean item = ((AaaContextImpl)c).getCached("ace_read|" + obj.getId());
		if (item != null)
			return item;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;

		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canRead(c,obj);
		((AaaContextImpl)c).setCached("ace_read|" + obj.getId(), MTimeInterval.MINUTE_IN_MILLISECOUNDS * 5, ret);
		return ret.bool;
	}

	protected boolean canUpdate(AaaContext c, DbMetadata obj)
			throws MException {
		
		Boolean item = ((AaaContextImpl)c).getCached("ace_update|" + obj.getId());
		if (item != null)
			return item;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;
		
		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canUpdate(c,obj);
		((AaaContextImpl)c).setCached("ace_update|" + obj.getId(),MTimeInterval.MINUTE_IN_MILLISECOUNDS * 5, ret);
		return ret.bool;
	}

	protected boolean canDelete(AaaContext c, DbMetadata obj)
			throws MException {
		
		Boolean item = ((AaaContextImpl)c).getCached("ace_delete"+ "|" + obj.getId());
		if (item != null)
			return item;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;

		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canDelete(c,obj);
		((AaaContextImpl)c).setCached("ace_delete|" + obj.getId(), MTimeInterval.MINUTE_IN_MILLISECOUNDS * 5,ret);
		return ret.bool;
	}

	protected boolean canCreate(AaaContext c, DbMetadata obj) throws MException {
		Boolean item = ((AaaContextImpl)c).getCached("ace_create"+ "|" + obj.getId());
		if (item != null)
			return item;
		
		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;
		
		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canCreate(c,obj);
		((AaaContextImpl)c).setCached("ace_create|" + obj.getId(),MTimeInterval.MINUTE_IN_MILLISECOUNDS * 5, ret);
		return ret.bool;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DbMetadata> T getObject(String type, UUID id) throws MException {
		DbSchemaService controller = getController(type);
		if (controller == null) return null;
		return (T) controller.getObject(type, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DbMetadata> T getObject(String type, String id) throws MException {
		DbSchemaService controller = getController(type);
		if (controller == null) return null;
		return (T) controller.getObject(type, id);
	}
	
	@Override
	public Set<Entry<String, DbSchemaService>> getController() {
		synchronized (controllers) {
			return controllers.entrySet();
		}
	}

	@Override
	public void onDelete(Persistable object) {

		if (object == null) return;
		
		ReferenceCollector collector = new ReferenceCollector() {
			LinkedList<UUID> list = new LinkedList<UUID>();
			@Override
			public void foundReference(Reference<?> ref) {
				if (ref.getType() == TYPE.CHILD) {
					if (ref.getObject() == null) return;
					// be sure not cause an infinity loop, a object should only be deleted once ...
					if (ref.getObject() instanceof UuidIdentificable) {
						if (list.contains(((UuidIdentificable)ref.getObject()).getId()))
							return;
						list.add(((UuidIdentificable)ref.getObject()).getId());
					}			
					// delete the object and dependencies
					try {
						doDelete(ref);
					} catch (MException e) {
						log().w("deletion failed",ref.getObject(),ref.getObject().getClass(),e);
					}
				}
			}
		};
		
		collectRefereces(object, collector);
	}

	protected void doDelete(Reference<?> ref) throws MException {
		log().d("start delete",ref.getObject(),ref.getType());
		onDelete(ref.getObject());
		log().d("delete",ref);
		getManager().delete(ref.getObject());
	}

	@Override
	public void collectRefereces(Persistable object, ReferenceCollector collector) {

		if (object == null) return;

		HashSet<DbSchemaService> distinct = new HashSet<DbSchemaService>();
		synchronized (controllers) {
			distinct.addAll(controllers.values());
		}

		for (DbSchemaService service : distinct)
			try {
				service.collectReferences(object, collector);
			} catch (Throwable t) {
				log().w(service.getClass(),object.getClass(),t);
			}
	}
	
	@Override
	public boolean canRead(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canRead(c, obj);
	}

	private AaaContext getCurrent() {
		return MApi.lookup(AccessApi.class).getCurrentOrGuest();
	}

	@Override
	public boolean canUpdate(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canUpdate(c, obj);
	}

	@Override
	public boolean canDelete(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canDelete(c, obj);
	}
	@Override
	public boolean canCreate(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c == null || c.isAdminMode()) return true;
		return canCreate(c, obj);
	}
	
	@Override
	public <T extends DbMetadata> T getObject(Class<T> type, UUID id) throws MException {
		return getObject(type.getCanonicalName(), id);
	}
	
	private class MyAccessTrackerCustomizer implements ServiceTrackerCustomizer<DbSchemaService,DbSchemaService>{

		@Override
		public DbSchemaService addingService(
				ServiceReference<DbSchemaService> reference) {

			DbSchemaService service = context.getService(reference);
			if (service != null) {
				LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
				service.registerObjectTypes(list);
				synchronized (controllers) {
					for (Class<?> clazz : list) {
						log().i("register access controller",clazz,service.getClass().getCanonicalName());
						DbSchemaService last = controllers.put(clazz.getCanonicalName(),service);
						if (last != null)
							log().w("overwrote access controller",clazz,service.getClass().getCanonicalName());
					}
				}
			}
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {

		}

		@Override
		public void removedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {
			
			if (service != null) {
				LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
				service.registerObjectTypes(list);
				synchronized (controllers) {
					for (Class<?> clazz : list) {
						log().i("remove access controller",clazz,service.getClass().getCanonicalName());
						controllers.remove(clazz.getCanonicalName());
					}
				}
			}
			
		}
		
	}


}
