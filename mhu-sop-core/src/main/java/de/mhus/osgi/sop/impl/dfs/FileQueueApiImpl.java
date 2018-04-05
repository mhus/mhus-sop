package de.mhus.osgi.sop.impl.dfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.MutableUri;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.dfs.DfsApi;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.dfs.FileQueueApi;
import de.mhus.osgi.sop.api.dfs.FileQueueOperation;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.util.SopUtil;

@Component(immediate=true)
public class FileQueueApiImpl extends MLog implements FileQueueApi {
	
	protected static long MAX_FILE_SIZE = 1024l * 1024l * 50l;
	protected static long MAX_FILE_AGE = MTimeInterval.MINUTE_IN_MILLISECOUNDS * 30;
	protected static final String READ_POINTER = "read_pointer";
	protected static int MAX_FILES = 1000 * 2; // 1000 queues, every queue needs 2 files

	static FileQueueApiImpl instance;

	@Activate
	public void doActivate(ComponentContext ctx) {
		instance = this;
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance = null;
	}
/*
	@Override
	public File loadFile(FileInfo info) throws MException {
		
		File dir = getUploadDir();
		File pFile = new File(dir, info.getFileReference() + ".properties");
		File dFile = new File(dir, info.getFileReference() + ".data");
		
		synchronized (this) {
			if (dFile.exists() && dFile.length() == info.getSize()) {
				// load cached
				MProperties prop = MProperties.load(pFile);
				if (prop.getLong("modified", -1) == info.getModified()) {
					prop.setLong("accessed", System.currentTimeMillis());
					try {
						prop.save(pFile);
					} catch (IOException e) {
						log().w(e);
					}
					return dFile;
				}
			}
			if (dFile.exists()) dFile.delete();
			if (pFile.exists()) pFile.delete();
		}
		
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		tags.add(OperationDescriptor.TAG_IDENT + "=" + info.getFileSource());
		OperationDescriptor desc = api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
		FileQueueOperation operation = OperationUtil.createOpertionProxy(FileQueueOperation.class, desc);
		File file = operation.getFile(info.getFileReference());
		
		synchronized (this) {
			file.renameTo(dFile);
			MProperties prop = new MProperties();
			prop.setString("name", info.getName());
			prop.setLong("size", info.getSize());
			prop.setLong("modified", info.getModified());
			prop.setLong("accessed", System.currentTimeMillis());
			try {
				prop.save(pFile);
			} catch (IOException e) {
				log().w(e);
			}
		}
		
		return file;
	}

	@Override
	public UploadFileInfo createQueueFile(String name, long ttl) {

		synchronized (this) {
			UUID id = UUID.randomUUID();
			
			File dir = getUploadDir();
			File pFile = new File(dir, id + ".properties");
			File dFile = new File(dir, id + ".data");
			
			MProperties prop = new MProperties();
			prop.setString("name", name);
			prop.setLong("accessed", System.currentTimeMillis());
			prop.setLong("expires", System.currentTimeMillis() + ttl);
			try {
				prop.save(pFile);
			} catch (IOException e) {
				log().w(e);
			}
			
			return new UploadFileInfoImpl(dFile, name, id);
		}
	}
*/
	public static File getUploadDir() {
		File dir = SopUtil.getFile("filequeue");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	public File getFile(UUID id) {
		File dir = getUploadDir();
//		File pFile = new File(dir, id + ".properties");
		synchronized (this) {
			File dFile = new File(dir, id + ".data");
			if (!dFile.exists()) return null;
			return dFile;
		}
	}

	@Override
	public UUID createQueueFile(String name, long ttl) throws IOException {
		
		if (ttl <= 0) ttl = DEFAULT_TTL;

		synchronized (this) {
			UUID id = UUID.randomUUID();
			
			File dir = getUploadDir();
			File pFile = new File(dir, id + ".properties");
			File dFile = new File(dir, id + ".data");
			dFile.createNewFile();
			
			MProperties prop = new MProperties();
			prop.setString("name", name);
			prop.setLong("created", System.currentTimeMillis());
			prop.setLong("accessed", System.currentTimeMillis());
			prop.setLong("expires", System.currentTimeMillis() + ttl);
			prop.setLong("ttl", ttl);
			prop.setBoolean("queued", true);

			prop.save(pFile);
			
			return id;
		}
	}

	@Override
	public UUID takeFile(File file, boolean copy, long ttl) throws IOException {
		
		if (ttl <= 0) ttl = DEFAULT_TTL;
		
		MProperties prop = new MProperties();
		prop.setString("name", file.getName());
		prop.setLong("modified", file.lastModified());

		UUID id = UUID.randomUUID();
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");

		if (copy)
			MFile.copyDir(file, dFile);
		else
			if (!file.renameTo(dFile))
				throw new IOException("Can't move file " + file + " to " + dFile);

		dFile.setWritable(false, false);

		synchronized (this) {
			prop.setLong("created", System.currentTimeMillis());
			prop.setLong("accessed", System.currentTimeMillis());
			prop.setLong("expires", System.currentTimeMillis() + ttl);
			prop.setLong("ttl", ttl);

			prop.save(pFile);
		}
		return id;
	}

	@Override
	public long closeQueueFile(UUID id) throws IOException {

		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");

		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		synchronized (this) {
			MProperties prop = MProperties.load(pFile);
			if (!prop.getBoolean("queued", false))
				throw new IOException("File not queued " + id);
			
			prop.setBoolean("queued", false);
			prop.setLong("expires", prop.getLong("ttl", DEFAULT_TTL) + System.currentTimeMillis());
			prop.setLong("accessed", System.currentTimeMillis());
			
			prop.save(pFile);
		}
		dFile.setWritable(false, false);
		return dFile.length();
	}

	@Override
	public long appendQueueFileContent(UUID id, byte[] content) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");

		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		synchronized (this) {
			MProperties prop = MProperties.load(pFile);
			if (!prop.getBoolean("queued", false))
				throw new IOException("File not queued " + id);
			
			FileOutputStream os = new FileOutputStream(dFile, true);
			os.write(content);
			os.close();
			
		}
		return dFile.length();
	}

	@Override
	public OutputStream createQueueFileOutputStream(UUID id) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");

		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		synchronized (this) {
			MProperties prop = MProperties.load(pFile);
			if (!prop.getBoolean("queued", false))
				throw new IOException("File not queued " + id);
			
			FileOutputStream os = new FileOutputStream(dFile);
			return os;
		}
	}

	@Override
	public File loadFile(MUri uri) throws IOException, MException {
		
		if (!DfsApi.SCHEME_DFQ.equals(uri.getScheme()))
			throw new IOException("Wrong scheme " + uri.getScheme() + " for queue");
		if (!MValidator.isUUID(uri.getPath()))
			throw new IOException("Malformed queue file id " + uri.getPath() );
		
		File dir = getUploadDir();
		File pFile = new File(dir, uri.getPath() + ".properties");
		File dFile = new File(dir, uri.getPath() + ".data");
		
		synchronized (this) {
			if (dFile.exists() && pFile.exists()) {
				// load cached
				MProperties prop = MProperties.load(pFile);
				if (prop.getBoolean("queued", false))
					throw new IOException("File queued " + uri);
				
				prop.setLong("accessed", System.currentTimeMillis());
				try {
					prop.save(pFile);
				} catch (IOException e) {
					log().w(e);
				}
				return dFile;
			}
			if (pFile.exists()) pFile.delete();
			if (dFile.exists()) {
				dFile.setWritable(true, false);
				dFile.delete();
			}
		}
		
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		OperationDescriptor desc = api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
		FileQueueOperation operation = OperationUtil.createOpertionProxy(FileQueueOperation.class, desc);
		FileInfo info = operation.getFileInfo(UUID.fromString(uri.getPath()));
		File file = operation.getFile(UUID.fromString(uri.getPath()));
		
		synchronized (this) {
			file.renameTo(dFile);
			dFile.setWritable(false, false);
			
			MProperties prop = new MProperties();
			prop.setString("name", info.getName());
			prop.setLong("size", info.getSize());
			prop.setLong("modified", info.getModified());
			prop.setLong("accessed", System.currentTimeMillis());
			try {
				prop.save(pFile);
			} catch (IOException e) {
				log().w(e);
			}
		}
		
		return file;
	}

	@Override
	public File loadFile(UUID id) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());

		synchronized (this) {
			MProperties prop = MProperties.load(pFile);
			if (prop.getBoolean("queued", false))
				throw new IOException("File queued " + id);
		}
		
		return dFile;
	}

	@Override
	public FileInfo getFileInfo(UUID id) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		
		MProperties prop = MProperties.load(pFile);

		return new FileInfoImpl(getUri(id), prop.getString("name", ""), dFile.length(), prop.getLong("modified", 0) );
	}

	@Override
	public MUri getUri(UUID id) throws FileNotFoundException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		
		MProperties prop = MProperties.load(pFile);
		
		MutableUri uri = new MutableUri(null);
		uri.setScheme(DfsApi.SCHEME_DFQ);
		uri.setLocation(SopUtil.getServerIdent());
		uri.setPath(id.toString());
		uri.setParams(new String[] { 
				String.valueOf(dFile.length()), 
				prop.getString("modified", "") 
			});
		
		return uri;
	}

	@Override
	public FileInfo getFileInfo(MUri uri) throws IOException, MException {
		if (!DfsApi.SCHEME_DFQ.equals(uri.getScheme()))
			throw new IOException("Wrong scheme " + uri.getScheme() + " for queue");
		if (!MValidator.isUUID(uri.getPath()))
			throw new IOException("Malformed queue file id " + uri.getPath() );
		
		File dir = getUploadDir();
		File pFile = new File(dir, uri.getPath() + ".properties");
		File dFile = new File(dir, uri.getPath() + ".data");
		
		synchronized (this) {
			if (dFile.exists() && pFile.exists()) {
				return new FileInfoImpl(getUri(UUID.fromString(uri.getPath())));
			}
		}
		
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		OperationDescriptor desc = api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
		FileQueueOperation operation = OperationUtil.createOpertionProxy(FileQueueOperation.class, desc);
		FileInfo info = operation.getFileInfo(UUID.fromString(uri.getPath()));
		return info;
		
	}

	public void cleanupQueue() {
		File dir = getUploadDir();
		synchronized (this) {
			for (File file : dir.listFiles()) {
				if (file.getName().endsWith(".properties")) {
					try {
						MProperties prop = MProperties.load(file);
						long expires = prop.getLong("expires", 0);
						long access = prop.getLong("access", 0);
						if (
								expires > 0 && System.currentTimeMillis() > expires ||
								expires == 0 && MTimeInterval.isTimeOut(access, FileQueueApi.DEFAULT_TTL) ||
								expires == 0 && access == 0
							) {
							String id = MString.beforeIndex(file.getName(), '.');
							log().d("cleanup", id, prop);
							File dFile = new File(dir, id + ".data");
							if (dFile.exists()) {
								dFile.setWritable(true, false);
								dFile.delete();
							}
							file.delete();
						}
					} catch (Throwable t) {
						log().e(file,t);
					}
				}
			}
		}
	}

	@Override
	public void touchFile(UUID id) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		
		MProperties prop = MProperties.load(pFile);
		synchronized (this) {
			
			prop.setLong("expires", prop.getLong("ttl", DEFAULT_TTL) + System.currentTimeMillis());
			prop.setLong("accessed", System.currentTimeMillis());
			
			prop.save(pFile);
		}
		
	}

	public Set<UUID> getQueuedIdList(boolean queued) {
		HashSet<UUID> out = new HashSet<>();
		File dir = getUploadDir();
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".properties")) {
				
				if (!queued) {
					MProperties prop = MProperties.load(file);
					if (prop.getBoolean("queued", false))
						continue;
				}
				
				String id = MString.beforeIndex(file.getName(), '.');
				out.add(UUID.fromString(id));
			}
		}
		return out;
	}

	public MProperties getProperties(UUID id) throws FileNotFoundException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		
		return MProperties.load(pFile);
	}

	public void delete(UUID id) {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		File dFile = new File(dir, id + ".data");
		if (pFile.exists()) pFile.delete();
		if (dFile.exists()) {
			dFile.setWritable(true, false);
			dFile.delete();
		}
	}

	public void setParameter(UUID id, String key, String val) throws IOException {
		File dir = getUploadDir();
		File pFile = new File(dir, id + ".properties");
		
		if (!pFile.exists()) throw new FileNotFoundException(id.toString());
		
		MProperties prop = MProperties.load(pFile);
		prop.put(key, val);
		prop.save(pFile);
	}

	public Set<String> listProviders() {
		HashSet<String> out = new HashSet<>();
		OperationApi api = MApi.lookup(OperationApi.class);
		for (OperationDescriptor desc : api.findOperations(FileQueueOperation.class.getCanonicalName(), null, null)) {
			String ident = OperationUtil.getOption(desc.getTags(), OperationDescriptor.TAG_IDENT, "");
			out.add(ident);
		}
		return out;
	}

	public FileQueueOperation getOperation(String ident) throws MException {
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		tags.add(OperationDescriptor.TAG_IDENT + "=" + ident);
		OperationDescriptor desc = api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
		FileQueueOperation operation = OperationUtil.createOpertionProxy(FileQueueOperation.class, desc);
		return operation;
	}
}
