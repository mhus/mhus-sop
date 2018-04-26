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
package de.mhus.osgi.sop.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgProperties;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.SopAcl;
import de.mhus.osgi.sop.impl.adb.SopDbImpl;

@Component(immediate=true,provide=SopApi.class,name="SopApi")
public class SopApiImpl extends MLog implements SopApi {

	@SuppressWarnings("unused")
	private BundleContext context;
	private CfgProperties config = new CfgProperties(SopApi.class, "sop");

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
	public IProperties getMainConfiguration() {
		return config.value();
	}

	@Override
	public PojoModelFactory getDataPojoModelFactory() {
		return SopDbImpl.getManager().getPojoModelFactory();
	}

	@Override
	public SopAcl getAcl(String id) throws MException {
		SopAcl acl = SopDbImpl.getManager().getObjectByQualification(Db.query(SopAcl.class).eq("target", id));
		return acl;
	}

	@Override
	public XdbService getManager() {
		return SopDbImpl.getManager();
	}

}