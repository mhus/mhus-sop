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

import java.util.List;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.adb.AbstractDbSchemaService;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.SopAcl;
import de.mhus.osgi.sop.api.model.SopActionTask;
import de.mhus.osgi.sop.api.model.SopObjectParameter;
import de.mhus.osgi.sop.api.model.SopRegister;

@Component(provide=DbSchemaService.class,immediate=true)
public class SopDbImpl extends AbstractDbSchemaService {

	private XdbService service;
	private UUID defFoundationId;
	private static SopDbImpl instance;

	public static SopDbImpl instance() {
		return instance;
	}
	
	@Override
	public void registerObjectTypes(List<Class<? extends Persistable>> list) {
		list.add(SopObjectParameter.class);
		list.add(SopActionTask.class);
		list.add(SopRegister.class);
		list.add(SopAcl.class);
	}

	@Override
	public void doInitialize(XdbService service) {
		this.service = service;
		instance = this;
	}

	@Override
	public void doPostInitialize(XdbService manager) throws Exception {
	}

	@Override
	public void doDestroy() {
		instance = null;
		service = null;
	}

	public static XdbService getManager() {
		return instance
				.service;
	}
	
//	@Override
//	public boolean canRead(AaaContext account, Persistable obj)
//			throws MException {
//		
//		if (obj instanceof SopObjectParameter) {
//			SopObjectParameter o = (SopObjectParameter)obj;
//			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
//			
//			String type = o.getObjectType();
//			if (type == null) return false;
//			if (type.equals(SopObjectParameter.class.getCanonicalName())) return true;
//			
//			
////			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
////			if (ace == null) return false;
////			return ace.canRead();
//			
//			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_READ, null);
//		}
//		if (obj instanceof SopFoundation) {
//			
//		}
//		if (obj instanceof SopFoundationGroup) {
//			return true;
//		}
//		if (obj instanceof FoundationRelated) {
//			UUID fId = ((FoundationRelated)obj).getFoundation();
//		}
//		
//		return false;
//	}

//	@Override
//	public boolean canUpdate(AaaContext account, Persistable obj)
//			throws MException {
//		if (obj instanceof SopObjectParameter) {
//			SopObjectParameter o = (SopObjectParameter)obj;
//			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
//			
//			String type = o.getObjectType();
//			if (type == null) return false;
//			if (type.equals(SopObjectParameter.class.getCanonicalName())) return true;
//			
////			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
////			if (ace == null) return false;
////			return ace.canUpdate();
//			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_UPDATE, null);
//
//		}
//		return false;
//	}

//	@Override
//	public boolean canDelete(AaaContext account, Persistable obj)
//			throws MException {
//		if (obj instanceof SopObjectParameter) {
//			SopObjectParameter o = (SopObjectParameter)obj;
//			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
//			
//			String type = o.getObjectType();
//			if (type == null) return false;
//			if (type.equals(SopObjectParameter.class.getCanonicalName())) return true;
//			
////			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
////			if (ace == null) return false;
////			return ace.canDelete();
//			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_DELETE, null);
//		}
//		return false;
//	}

//	@Override
//	public boolean canCreate(AaaContext account, Persistable obj)
//			throws MException {
//		
//		if (obj instanceof SopActionTask)
//			return true;
//		
//		if (obj instanceof SopObjectParameter) {
//			SopObjectParameter o = (SopObjectParameter)obj;
//			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
//			
//			String type = o.getObjectType();
//			if (type == null) return false;
//			if (type.equals(SopObjectParameter.class.getCanonicalName())) return true;
//			
////			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
////			if (ace == null) return false;
////			return ace.canCreate();
//			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_CREATE, null);
//		}
//		
//		return false;
//	}

//	@Override
//	public Persistable getObject(String type, UUID id) throws MException {
//		if (type.equals(SopObjectParameter.class.getCanonicalName()))
//			return SopDbImpl.getManager().getObject(SopObjectParameter.class, id);
//		if (type.equals(SopActionTask.class.getCanonicalName()))
//			return SopDbImpl.getManager().getObject(SopActionTask.class, id);
//		if (type.equals(SopFoundation.class.getCanonicalName()))
//			return SopDbImpl.getManager().getObject(SopFoundation.class, id);
//		if (type.equals(SopFoundationGroup.class.getCanonicalName()))
//			return SopDbImpl.getManager().getObject(SopFoundationGroup.class, id);
//		if (type.equals(SopJournal.class.getCanonicalName()))
//			return SopDbImpl.getManager().getObject(SopJournal.class, id);
////		if (type.equals(Register.class.getCanonicalName()))
////			return SopDbImpl.getManager().getObject(Register.class, id);
//		throw new MException("unknown type",type);
//	}

//	@Override
//	public Persistable getObject(String type, String id) throws MException {
//		return getObject(type, UUID.fromString(id));
//	}

	@Override
	public void collectReferences(Persistable object,
			ReferenceCollector collector) {
		if (object == null || !(object instanceof UuidIdentificable)) return;
		UuidIdentificable meta = (UuidIdentificable)object;
		try {
			for (SopObjectParameter p : MApi.lookup(AdbApi.class).getParameters(object.getClass(), meta.getId())) {
				collector.foundReference(new Reference<Persistable>(p,TYPE.CHILD));
			}
		} catch (MException e) {
			log().d(object.getClass(),meta.getId(),e);
		}
	}

	@Override
	public void doCleanup() {
		
	}

	public UUID getDefaultFoundationId() {
		return defFoundationId;
	}

	@Override
	protected String getAcl(AaaContext context, Persistable obj) throws MException {
		
		return null;
	}

	@Override
	public boolean canCreate(AaaContext context, Persistable obj) throws MException {
		if (obj == null) return false;
		if (context.isAdminMode()) return true;
		return false;
	}
	
}
