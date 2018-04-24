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

import java.util.TreeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.lib.sql.DataSourceProvider;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.PseudoDbPool;
import de.mhus.osgi.services.adb.DbManagerService;
import de.mhus.osgi.services.adb.DbManagerServiceImpl;
import de.mhus.osgi.sop.api.adb.DbSchemaService;

@Component(provide=DbManagerService.class,immediate=true)
public class SopDbManagerService extends DbManagerServiceImpl {
	
	private ServiceTracker<DbSchemaService,DbSchemaService> tracker;
	private TreeMap<String,DbSchemaService> schemaList = new TreeMap<>();

	private BundleContext context;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
//		new de.mhus.lib.adb.util.Property();
		context = ctx.getBundleContext();
		tracker = new ServiceTracker<>(context, DbSchemaService.class, new MyTrackerCustomizer() );
		tracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
//		super.doDeactivate(ctx);
		tracker.close();
		tracker = null;
		context = null;
		schemaList.clear();
	}

	@Override
	protected DbSchema doCreateSchema() {
		return new SopDbSchema(this);
	}

	@Override
	public void doInitialize() throws MException {
		setDataSourceName(MApi.getCfg(DbManagerService.class).getExtracted("dataSourceName", "db_sop") );
	}

	@Override
	protected DbPool doCreateDataPool() {
		return new PseudoDbPool(new DataSourceProvider(getDataSource(), doCreateDialect(), doCreateConfig(), doCreateActivator() ));
	}
	
	private class MyTrackerCustomizer implements ServiceTrackerCustomizer<DbSchemaService, DbSchemaService> {

		@Override
		public DbSchemaService addingService(ServiceReference<DbSchemaService> reference) {

			DbSchemaService service = context.getService(reference);
			String name = service.getClass().getCanonicalName();
			service.doInitialize(SopDbManagerService.this.getManager());

			synchronized (schemaList) {
				schemaList.put(name, service);
				updateManager();
			}	
			
			if (SopDbManagerService.this.getManager() != null) {
				// already open
				log().d("addingService","doPostInitialize",name);
				try {
					service.doPostInitialize(SopDbManagerService.this.getManager());
				} catch (Throwable t) {
					log().w(name,t);
				}
			}
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {

			synchronized (schemaList) {
				updateManager();
			}
		}

		@Override
		public void removedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {

			String name = service.getClass().getCanonicalName();
			service.doDestroy();
			synchronized (schemaList) {
				schemaList.remove(name);
				updateManager();
			}
		}
		
	}

	protected void updateManager() {
		try {
			DbManager m = getManager();
			if (m != null)
				m.reconnect();
		} catch (Exception e) {
			log().e(e);
		}
	}

	public DbSchemaService[] getSchemas() {
		synchronized (schemaList) {
			return schemaList.values().toArray(new DbSchemaService[schemaList.size()]);
		}
	}

	@Override
	public String getServiceName() {
		return MApi.getCfg(DbManagerService.class).getString("serviceName", "sop");
	}

	@Override
	protected void doPostOpen() throws MException {
		synchronized (schemaList) {
			schemaList.forEach((name,service) -> {
				log().d("doPostOpen","doPostInitialize",name);
				try {
					service.doPostInitialize(getManager());
				} catch (Exception e) {
					log().e("doPostInitialize",name,e);
				}
			});
		}
	}

}
