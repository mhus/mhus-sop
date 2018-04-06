package de.mhus.osgi.sop.impl.dfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationToIfcProxy;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.MutableUri;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.dfs.DfsProviderOperation;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.dfs.FileQueueApi;
import de.mhus.osgi.sop.api.util.SopUtil;

@Component(provide=Operation.class,immediate=true)
public class DfsSopProvider extends OperationToIfcProxy implements DfsProviderOperation {

	private SoftHashMap<String, UUID> queueCache = new SoftHashMap<>();
	
	@Override
	public FileInfo getFileInfo(MUri uri) {
		File dir = getDirectory();
		File file = new File (dir, MFile.normalizePath(uri.getPath()) );
		if (!file.exists() && !file.isFile()) return null;
		
		MutableUri u =new MutableUri(uri.toString());
		u.setParams(new String[] {
				String.valueOf(file.length()),
				String.valueOf(file.lastModified())
			});
		return new FileInfoImpl(u);
	}

	@Override
	public MUri exportFile(MUri uri) throws IOException {
		File dir = getDirectory();
		File file = new File (dir, MFile.normalizePath(uri.getPath()) );
		if (!file.exists() && !file.isFile()) return null;
		
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		UUID id = null;
		synchronized (queueCache) {
			id = queueCache.get(file.getCanonicalPath());
		}
		if (id != null) {
			try {
				FileInfo localInfo = api.getFileInfo(id);
				if (localInfo.getModified() == file.lastModified()) {
					api.touchFile(id, 0);
					return MUri.toUri(localInfo.getUri());
				}
			} catch (FileNotFoundException fnf) {
				
			}
		}
		id = api.takeFile(file, true, 0);
		synchronized (queueCache) {
			queueCache.put(file.getCanonicalPath(), id);
		}
		
		return api.getUri(id);
	}

	@Override
	public Map<String, MUri> getDirectoryList(MUri uri) {
		File dir = getDirectory();
		dir = new File (dir, MFile.normalizePath(uri.getPath()) );
		TreeMap<String,MUri> out = new TreeMap<>();
		if (!dir.exists()) return out;
		for (File file : dir.listFiles()) {
			if (!file.isHidden() && !file.getName().startsWith(".")) {
				MutableUri u = new MutableUri(null);
				u.setScheme(uri.getScheme());
				u.setLocation(uri.getLocation());
				u.setPath( uri.getPath() + "/" + file.getName());
				out.put(file.getName() + (file.isDirectory() ? "/" : ""), u);
			}
		}
		return out;
	}

	private File getDirectory() {
		File ret = SopUtil.getFile("dfs");
		if (!ret.exists()) ret.mkdirs();
		return ret;
	}

	@Override
	protected Class<?> getInterfaceClass() {
		return DfsProviderOperation.class;
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
		parameters.put(PARAM_SCHEME, "sop");
		parameters.put(OperationDescription.TAGS, "acl=*");
	}

	@Override
	public void importFile(MUri queueUri, MUri target) throws IOException {
		if (!AaaUtil.isCurrentAdmin())
			throw new IOException("Not supported");
		
		File dir = getDirectory();
		File file = new File (dir, MFile.normalizePath(target.getPath()) );
		dir = file.getParentFile();
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Directory not found");
		
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		File fromFile;
		try {
			fromFile = api.loadFile(queueUri);
		} catch (MException e) {
			throw new IOException(e);
		}
		MFile.copyFile(fromFile, file);
	}

	@Override
	public void deleteFile(MUri uri) throws IOException {
		if (!AaaUtil.isCurrentAdmin())
			throw new IOException("Not supported");
		
		File dir = getDirectory();
		File file = new File (dir, MFile.normalizePath(uri.getPath()) );

		MFile.deleteDir(file);
	}

	@Override
	public void createDirecories(MUri uri) throws IOException {
		if (!AaaUtil.isCurrentAdmin())
			throw new IOException("Not supported");
		
		File dir = getDirectory();
		dir = new File (dir, MFile.normalizePath(uri.getPath()) );
		
		if (dir.exists() && dir.isFile())
			throw new IOException("file exists");
		dir.mkdirs();
	}

}
