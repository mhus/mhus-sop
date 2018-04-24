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
package de.mhus.osgi.sop.impl.dfs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationToIfcProxy;
import de.mhus.lib.core.util.Version;
import de.mhus.osgi.services.MOsgi;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.dfs.FileQueueOperation;

@Component(provide=Operation.class)
public class FileQueueOperationImpl extends OperationToIfcProxy implements FileQueueOperation {

	@Override
	public File getFile(UUID id) {
		return FileQueueApiImpl.instance.getFile(id);
	}

	@Override
	public FileInfo getFileInfo(UUID id) {
		try {
			return FileQueueApiImpl.instance.getFileInfo(id);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected Class<?> getInterfaceClass() {
		return FileQueueOperation.class;
	}

	@Override
	protected Object getInterfaceObject() {
		return this;
	}

	@Override
	protected Version getInterfaceVersion() {
		return MOsgi.getBundelVersion(this.getClass());
	}

	@Override
	protected void initOperationDescription(HashMap<String, String> parameters) {
		parameters.put(OperationDescription.TAGS, "acl=*");
	}

	@Override
	public Set<UUID> getQueuedIdList() {
		return FileQueueApiImpl.instance.getQueuedIdList(false);
	}

}
