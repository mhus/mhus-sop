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

import java.util.List;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.basics.Ace;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.adb.AbstractDbSchemaService;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.foundation.FoundationRelated;
import de.mhus.osgi.sop.api.foundation.model.SopData;
import de.mhus.osgi.sop.api.foundation.model.SopFoundation;
import de.mhus.osgi.sop.api.foundation.model.SopFoundationGroup;
import de.mhus.osgi.sop.api.foundation.model.SopJournal;
import de.mhus.osgi.sop.api.model.SopAcl;
import de.mhus.osgi.sop.api.model.SopObjectParameter;

@Component(provide=DbSchemaService.class,immediate=true)
public class FoundationDbImpl extends AbstractDbSchemaService {

	private XdbService service;
	private UUID defFoundationId;
	private static FoundationDbImpl instance;

	public static FoundationDbImpl instance() {
		return instance;
	}
	
	@Override
	public void registerObjectTypes(List<Class<? extends Persistable>> list) {
		list.add(SopJournal.class);
		list.add(SopFoundation.class);
		list.add(SopFoundationGroup.class);
		list.add(SopData.class);
	}

	@Override
	public void doInitialize(XdbService service) {
		this.service = service;
		instance = this;
	}

	@Override
	public void doPostInitialize(XdbService manager) throws Exception {
		
		AaaUtil.enterRoot();
		try {
			// init base structure
			SopFoundationGroup defGroup = service.getObjectByQualification(Db.query(SopFoundationGroup.class).eq("name", ""));
			if (defGroup == null) {
				defGroup = service.inject(new SopFoundationGroup(""));
				defGroup.save();
			}
			
			SopFoundation defFound = service.getObjectByQualification(Db.query(SopFoundation.class).eq("ident", ""));
			if (defFound == null) {
				defFound = service.inject(new SopFoundation("",""));
				defFound.save();
			}
			defFoundationId = defFound.getId();
			try {
				SopAcl acl = service.getObjectByQualification(Db.query(SopAcl.class).eq("target", defFoundationId.toString()));
				if (acl == null) {
					acl = service.inject(new SopAcl(defFoundationId.toString(), "*=r"));
					acl.save();
				}
			} catch (Throwable t) {
				log().w(t);
			}
			try {
				SopAcl acl = service.getObjectByQualification(Db.query(SopAcl.class).eq("target", defFoundationId + "_"));
				if (acl == null) {
					acl = service.inject(new SopAcl(defFoundationId.toString() + "_", "*=r"));
					acl.save();
				}
			} catch (Throwable t) {
				log().w(t);
			}
		} finally {
			AaaUtil.leaveRoot();
		}
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
	public String getAcl(AaaContext context, Persistable obj) throws MException {
		
		if (obj instanceof SopFoundationGroup) {
			return Ace.RIGHTS_RO;
		}
		if (obj instanceof FoundationRelated) {
			UUID fId = ((FoundationRelated)obj).getFoundation();
			SopFoundation f = getManager().getObject(SopFoundation.class, fId);
			if (f == null) return Ace.RIGHTS_NONE;
			{
				SopAcl aclObject = MApi.lookup(AdbApi.class).getManager().getObjectByQualification(Db.query(SopAcl.class).eq("target", fId + "_"+obj.getClass().getSimpleName() ));
				if (aclObject != null)
					return aclObject.getList();
			}
			{
				SopAcl aclObject = MApi.lookup(AdbApi.class).getManager().getObjectByQualification(Db.query(SopAcl.class).eq("target", fId + "_" ));
				if (aclObject != null)
					return aclObject.getList();
			}
			return getAce(context,f).getRights();
		}
		
		return null;
	}

	@Override
	public boolean canCreate(AaaContext context, Persistable obj) throws MException {
		if (obj == null) return false;
		if (context.isAdminMode()) return true;
		if (obj instanceof FoundationRelated) {
			UUID fId = ((FoundationRelated)obj).getFoundation();
			SopFoundation f = getManager().getObject(SopFoundation.class, fId);
			if (f == null) return false;
			{
				SopAcl aclObject = MApi.lookup(AdbApi.class).getManager().getObjectByQualification(Db.query(SopAcl.class).eq("target", fId + "_" + obj.getClass().getSimpleName() ));
				if (aclObject != null) {
					Ace ace = AaaUtil.getAccessAce(context, aclObject.getList());
					return ace.canCreate();
				}
			}
			{
				SopAcl aclObject = MApi.lookup(AdbApi.class).getManager().getObjectByQualification(Db.query(SopAcl.class).eq("target", fId + "_" ));
				if (aclObject != null) {
					Ace ace = AaaUtil.getAccessAce(context, aclObject.getList());
					return ace.canCreate();
				}
			}
		}
		return false;
	}
	
}
