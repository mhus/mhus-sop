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
package de.mhus.osgi.sop.impl.util;

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.strategy.AbstractOperation;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.core.strategy.TaskContext;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.util.SopUtil;

@Component(service=Operation.class, properties="tags=acl=*")
public class PingOperation extends AbstractOperation {

	@Override
	protected OperationResult doExecute2(TaskContext context) throws Exception {
		log().i("PING PONG", context.getParameters() );
		String user = "";
		boolean admin = false;
		try {
			AccessApi aaa = MApi.lookup(AccessApi.class);
			AaaContext c = aaa.getCurrentOrGuest();
			user = c.getAccountId();
			admin = c.isAdminMode();
		} catch (Throwable t) {}
		
		String ident = SopUtil.getServerIdent();
		String pid = MSystem.getPid();
		String host = MSystem.getHostname();
		String free = MSystem.freeMemoryAsString();
		long time = System.currentTimeMillis();
		
		return new Successful(this, "ok", "user", user, "admin", ""+admin, "ident", ident, "pid", pid, "host", host, "free", free, "time", ""+time);
	}

	@Override
	protected OperationDescription createDescription() {
		return new OperationDescription(getUuid(),PingOperation.class, this, "Ping");
	}



}
