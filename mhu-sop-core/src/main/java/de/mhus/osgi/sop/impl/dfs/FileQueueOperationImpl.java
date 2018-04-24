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
