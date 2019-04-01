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
package de.mhus.osgi.sop.foundation;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.adb.query.SearchHelper;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.services.MOsgi;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.data.SopDataController;
import de.mhus.osgi.sop.api.foundation.FoundationApi;
import de.mhus.osgi.sop.api.foundation.model.SopData;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.foundation.model.SopFoundationGroup;
import de.mhus.osgi.sop.api.foundation.model.SopJournal;
import de.mhus.osgi.sop.api.foundation.model._SopData;
import de.mhus.osgi.sop.api.foundation.model._SopFoundation;
import de.mhus.osgi.sop.api.foundation.model._SopJournal;
import de.mhus.osgi.sop.api.rest.RestUtil;

@Component(immediate=true,name="FoundationApi")
public class FoundationApiImpl extends MLog implements FoundationApi {

	@SuppressWarnings("unused")
	private BundleContext context;
	private long lastOrder = 0;
	private long lastOrderTime = 0;
	
	
	private static final SearchHelper SEARCH_HELPER_FOUNDATION = new SearchHelper() {
		@Override
		public String findKeyForValue(AQuery<?> query, String value) {
			if (MValidator.isUUID(value))
				return "id";
			return "ident";
		}
	};

	private static final SearchHelper SEARCH_HELPER_DATA = new SearchHelper() {
		@Override
		public String findKeyForValue(AQuery<?> query, String value) {
			if (MValidator.isUUID(value))
				return "id";
			return "foreignid";
		}
	};

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		// TODO set synchronizer
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		context = null;
	}
	
	@Override
	public SopJournal appendJournalEntry(UUID foundation, String queue, String event, String... data) throws MException {
		SopJournal item = getManager().inject(new SopJournal(foundation, queue,event,getJournalOrder(),data));
		item.save();
		return item;
	}

	@Override
	public SopJournal appendJournalEntry(UUID foundation, String queue, String event, Map<String, Object> data) throws MException {
		SopJournal item = getManager().inject(new SopJournal(foundation, queue,event,getJournalOrder(),data));
		item.save();
		return item;
	}
	
	private synchronized long getJournalOrder() {
		long cur = System.currentTimeMillis();
		if (cur != lastOrderTime) {
			lastOrderTime  = cur;
			lastOrder = lastOrderTime * 1000;
		} else 
		if (lastOrder % 1000 == 999) {
			// overrun ...
			log().w("journal order overrun");
		} else
		{
			lastOrder++;
		}
		return lastOrder;
	}

	@Override
	public SopJournal getJournalEntry(String id) throws MException {
		long order = MCast.tolong(id, 0);
		if (order > 0)
			return getManager().getObjectByQualification(Db.query(SopJournal.class).eq("order", order));
		if (MValidator.isUUID(id))
			return getManager().getObject(SopJournal.class, UUID.fromString(id));
		return null;
	}

	@Override
	public List<SopJournal> getJournalEntries(UUID foundation, String queue, long since, int max, int page, String search) throws MException {
		
		AQuery<SopJournal> query = Db.query(SopJournal.class).eq("queue", queue).eq("foundation", foundation);
		if (since > 0)
			query.gt(_SopJournal._ORDER, since);
		if (MString.isSet(search))
			query.or(Db.like("event", "%" + search + "%"),Db.like("data", "%" + search + "%"));
		query.desc("order");
		
		
		DbCollection<SopJournal> res = getManager().getByQualification(query);
		res.skip(page * max);
		
		int cnt = 0;
		LinkedList<SopJournal> out = new LinkedList<SopJournal>();
		for (SopJournal j : res) {
			out.add(j);
			cnt++;
			if (cnt >= max) {
				res.close();
				break;
			}
		}
		return out;
	}

	@Override
	public SopFoundationGroup getFoundationGroup(String group) throws MException {
		return getManager().getObjectByQualification(Db.query(SopFoundationGroup.class).eq("name", group));
	}

	@Override
	public SopFoundation getFoundation(UUID id) throws MException {
		return getManager().getObject(SopFoundation.class, id);
	}

	@Override
	public UUID getDefaultFoundationId() {
		return FoundationDbImpl.instance().getDefaultFoundationId();
	}

	public XdbService getManager() {
		return MApi.lookup(SopApi.class).getManager();
	}

	@Override
	public SopDataController getDataSyncControllerForType(String type) {
		try {
			SopDataController out = MOsgi.getService(SopDataController.class, "(type=" + type + ")");
			return out;
		} catch (NotFoundException e) {
			return null;
		}
	}

	@Override
	public List<SopData> getSopData(UUID foundId, String type, String search, boolean publicAccess, Boolean archived,
	        Date due, String order, int size, int page) throws MException {
		
		AQuery<SopData> query = Db.query(SopData.class);
		if (type != null)
			query.eq(_SopData._TYPE, type);
		if (foundId != null)
			query.eq(_SopData._FOUNDATION, foundId);
		if (publicAccess)
//			query.eq(Db.attr("ispublic"), Db.value(true));
			query.eq(_SopData._IS_PUBLIC, true);
		if (due != null)
			query.lt(_SopData._DUE, due);

		boolean isArchived = false;
		
		if (MString.isSet(search)) {
			for (String part : search.split(" ")) {
				part = part.trim();
				if (MString.isIndex(part, ':')) {
					String key = MString.beforeIndex(part, ':');
					String val = MString.afterIndex(part, ':');
					
					switch(key) {
					case "value0":
						query.eq(_SopData._VALUE0,val);break;
					case "value1":
						query.eq(_SopData._VALUE1,val);break;
					case "value2":
						query.eq(_SopData._VALUE2,val);break;
					case "value3":
						query.eq(_SopData._VALUE3,val);break;
					case "value4":
						query.eq(_SopData._VALUE4,val);break;
					case "value5":
						query.eq(_SopData._VALUE5,val);break;
					case "value6":
						query.eq(_SopData._VALUE6,val);break;
					case "value7":
						query.eq(_SopData._VALUE7,val);break;
					case "value8":
						query.eq(_SopData._VALUE8,val);break;
					case "value9":
						query.eq(_SopData._VALUE9,val);break;
					case "*value0*":
						query.like(_SopData._VALUE0,"%"+val+"%");break;
					case "*value1*":
						query.like(_SopData._VALUE1,"%"+val+"%");break;
					case "*value2*":
						query.like(_SopData._VALUE2,"%"+val+"%");break;
					case "*value3*":
						query.like(_SopData._VALUE3,"%"+val+"%");break;
					case "*value4*":
						query.like(_SopData._VALUE4,"%"+val+"%");break;
					case "*value5*":
						query.like(_SopData._VALUE5,"%"+val+"%");break;
					case "*value6*":
						query.like(_SopData._VALUE6,"%"+val+"%");break;
					case "*value7*":
						query.like(_SopData._VALUE7,"%"+val+"%");break;
					case "*value8*":
						query.like(_SopData._VALUE8,"%"+val+"%");break;
					case "*value9*":
						query.like(_SopData._VALUE9,"%"+val+"%");break;
					case "writable":
						query.eq(_SopData._IS_WRITABLE, MCast.toboolean(val, false));break;
					case "archived":
						isArchived = true;
						if (!"all".equals(val))
							query.eq(_SopData._ARCHIVED, MCast.toboolean(val, false));
						break;
					case "status":
						query.eq("status", val); break;
					}
					
	//				if (key.equals("archive") && MCast.toboolean(val, false))
	//					query.eq("archived", true);
						
				} else {
					query.or( 
							Db.eq(_SopData._FOREIGN_ID, part), 
							Db.like(_SopData._VALUE0, "%"+part+"%"), 
							Db.like(_SopData._VALUE1, "%"+part+"%"),
							Db.like(_SopData._VALUE2, "%"+part+"%"),
							Db.like(_SopData._VALUE3, "%"+part+"%"),
							Db.like(_SopData._VALUE4, "%"+part+"%"),
							Db.like(_SopData._VALUE5, "%"+part+"%"),
							Db.like(_SopData._VALUE6, "%"+part+"%") ,
							Db.like(_SopData._VALUE7, "%"+part+"%"),
							Db.like(_SopData._VALUE8, "%"+part+"%"),
							Db.like(_SopData._VALUE9, "%"+part+"%") );
				}
			}
		}

		if (!isArchived)
			if (archived != null)
				query.eq(_SopData._ARCHIVED, archived.booleanValue());

		if (order == null)
			query.desc("foreignid");
		else {
			boolean asc = true;
			if (order.endsWith(" desc")) {
				asc = false;
				order = MString.beforeLastIndex(order, ' ');
			} else
			if (order.endsWith(" asc")) {
				order = MString.beforeLastIndex(order, ' ');
			}
			if (asc)
				query.asc(order);
			else
				query.desc(order);
		}
		LinkedList<SopData> out = RestUtil.collectResults(getManager(),query,page, size);
		return out;
	}

	@Override
	public SopData getSopData(UUID foundId, UUID id, boolean sync) throws MException {
		SopData out = getManager().getObject(SopData.class, id);
		
		if (out == null) return null;
		if (foundId != null && !out.getFoundation().equals(foundId))
			return null;
		
		if (sync)
			syncSopData(out, false, true);
		
		return out;
	}

	@Override
	public SopData getSopData(UUID foundId, String id, boolean sync) throws MException {
		SopData out = null;
		if (MValidator.isUUID(id)) {
			out = getManager().getObject(SopData.class, UUID.fromString(id));
		} else {
			out = getManager().getObjectByQualification(
					Db.extendObjectQueryFromSearch(
							Db.query(SopData.class),
							id,
							SEARCH_HELPER_DATA
					));
		}
		if (out == null) return null;
		if (foundId != null && !out.getFoundation().equals(foundId))
			return null;
		
		if (sync)
			syncSopData(out, false, true);
		
		return out;
	}

	@Override
	public SopData getSopDataByForeignId(UUID orgaId, String type, String id) throws MException {
		return getManager().getObjectByQualification(Db.query(SopData.class)
				.eq(M.n(_SopData._FOUNDATION), orgaId)
				.eq(M.n(_SopData._TYPE), type)
				.eq(M.n(_SopData._FOREIGN_ID), id));
	}

	@Override
	public boolean syncSopData(SopData obj, boolean forced, boolean save) {
		
		if (obj == null) return false;
		
		SopDataController sync = getDataSyncControllerForType(obj.getType());
		if (sync == null) {
			log().t("Synchronizer for type not found",obj);
			return false;
		}
		
		if (!forced) {
			if (!sync.isNeedSync(obj))
				return false;
		}
		
		log().d("Synchronize SopData",obj);
		obj.setLastSyncMsg("ok", false);
		try {
			sync.synchronizeSopData(obj);
		} catch (Throwable t) {
			log().w("SopData sync failed",obj,t);
			obj.setLastSyncTry(true);
			obj.setLastSyncMsg(t.toString(), true);
			return false;
		}
		
		obj.setLastSyncTry(false);
		obj.setLastSync(obj.getLastSyncTry());
		try {
			if (save)
				obj.save();
		} catch (MException e) {
			log().w(obj,e);
		}
		return true;
		
	}

	@Override
	public List<SopFoundation> searchFoundations(String search, int page) throws MException {

		AaaContext context = MApi.lookup(AccessApi.class).getCurrent();
		
		if (!context.isAdminMode()) {
			List<UUID> list = FoundationContextCacheService.get(context, SopFoundation.class.getCanonicalName() + "_" + search );
			if (list != null) {
				LinkedList<SopFoundation> out = new LinkedList<>();
				for (UUID id : list) {
					SopFoundation o = getFoundation(id.toString());
					if (o != null)
						out.add(o);
				}
				return out;
			}
		}
		// cache.get( );
		
		LinkedList<SopFoundation> out = RestUtil.collectResults(
				getManager(),
				Db.extendObjectQueryFromSearch(
					Db.query(SopFoundation.class).eq(_SopFoundation._ACTIVE, true),
					search,
					SEARCH_HELPER_FOUNDATION
				)
				,
				page,
				0
				);
		
		if (!context.isAdminMode() && out.size() < RestUtil.MAX_RETURN_SIZE ) {
			LinkedList<UUID> cached = new LinkedList<UUID>();
			for (SopFoundation o : out)
				cached.add(o.getId());
			
			FoundationContextCacheService.set(context, SopFoundation.class.getCanonicalName() + "_" + search, MPeriod.HOUR_IN_MILLISECOUNDS , cached );
		}

		
		return out;
	}

	@Override
	public SopFoundation getFoundation(String id) throws MException {
		if (MValidator.isUUID(id))
			return getManager().getObject(SopFoundation.class, UUID.fromString(id));

		return getManager().getObjectByQualification(
				Db.extendObjectQueryFromSearch(
						Db.query(SopFoundation.class),
						id,
						SEARCH_HELPER_FOUNDATION
				));
	}
	
}