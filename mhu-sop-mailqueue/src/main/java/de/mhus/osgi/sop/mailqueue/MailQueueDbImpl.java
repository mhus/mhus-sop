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
package de.mhus.osgi.sop.mailqueue;

import java.util.List;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.basics.AclControlled;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.adb.AbstractDbSchemaService;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;

@Component(provide=DbSchemaService.class,immediate=true)
public class MailQueueDbImpl extends AbstractDbSchemaService {

	@Override
	public void registerObjectTypes(List<Class<? extends Persistable>> list) {
		list.add(SopMailTask.class);
	}

	@Override
	public void doInitialize(XdbService dbService) {
		
	}

	@Override
	public void doDestroy() {
		
	}

	@Override
	public boolean canCreate(AaaContext context, Persistable obj) throws MException {
		return true; // TODO can everybody create mail queue items?
	}

	@Override
	public void collectReferences(Persistable object, ReferenceCollector collector) {
		
	}

	@Override
	public void doCleanup() {
		
	}

	@Override
	public void doPostInitialize(XdbService manager) throws Exception {
		
	}

	@Override
	public String getAcl(AaaContext context, Persistable obj) throws MException {
		return AclControlled.ACL_ALL_ALL;
	}

}
