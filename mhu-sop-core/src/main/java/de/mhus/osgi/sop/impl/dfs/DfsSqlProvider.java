package de.mhus.osgi.sop.impl.dfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.sql.DataSource;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.config.XmlConfigFile;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationToIfcProxy;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.MutableUri;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.sql.DataSourceProvider;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.DbResult;
import de.mhus.lib.sql.DbStatement;
import de.mhus.lib.sql.DefaultDbPool;
import de.mhus.osgi.services.MOsgi;
import de.mhus.osgi.services.util.DataSourceUtil;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.dfs.DfsProviderOperation;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.dfs.FileQueueApi;

public class DfsSqlProvider extends OperationToIfcProxy implements DfsProviderOperation {
	
	private String scheme = "sql";
	private String prefix = "sop_dfs";
	private String acl = "*";
	private String dataSourceName = "sop";
	private DbPool pool;
	private SoftHashMap<String, UUID> queueCache = new SoftHashMap<>();
	private DataSource dataSource;
	private DataSourceProvider dsProvider;

	public DfsSqlProvider(String dataSource, String scheme, String prefix, String acl) {
		this.dataSourceName = dataSource;
		this.scheme = scheme;
		this.prefix = prefix;
		this.acl = acl;
	}
	
	@Override
	public FileInfo getFileInfo(MUri uri) {
		init();
		EntryData entry = getEntry(uri);
		if (entry == null) return null;
		
		MutableUri u =new MutableUri(uri.toString());
		u.setParams(new String[] {
				String.valueOf(entry.size),
				String.valueOf(entry.modified.getTime())
			});
//		HashMap<String,String> query = new HashMap<>();
//		query.put("created", String.valueOf(entry.created.getTime()));
//		u.setQuery(query);
		return new FileInfoImpl(u);
	}

	private EntryData getEntry(MUri uri) {
		String path = normalizePath(uri.getPath());
		return getEntry(path);
	}
	
	private EntryData getEntry(String path) {
		init();
		DbConnection con = null;
		DbResult res = null;
		try {
			con = pool.getConnection();
			DbStatement sth = con.createStatement("SELECT id,name,path,created,modified,pathlevel,size FROM " + prefix + "_entry WHERE path = $path$");
			MProperties prop = new MProperties();
			prop.setString("path", path);
			res = sth.executeQuery(prop);
			if (res.next()) {
				EntryData entry = new EntryData(res);
				if (res.next()) {
					log().w("more then one entry for",path);
				}
				return entry;
			}
			return null;
		} catch (Exception e) {
			log().e(path,e);
			return null;
		} finally {
			if (res != null) try {res.close();} catch (Throwable t) {log().e(t);};
			if (con != null) con.close();
		}
	}

	@Override
	public MUri exportFile(MUri uri) throws IOException {
		init();
		
		String path = normalizePath(uri.getPath());
		UUID id = null;
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		if (api == null) throw new IOException("FileQueueApi not found");
		
		// from cache ?
		synchronized (queueCache) {
			id = queueCache.get(path);
		}
		if (id != null) {
			try {
				EntryData entry = getEntry(uri);
				if (entry == null) throw new IOException("Entry not found " + uri);
				
				FileInfo localInfo = api.getFileInfo(id);
				if (localInfo.getModified() == entry.modified.getTime()) {
					api.touchFile(id, 0);
					return MUri.toUri(localInfo.getUri());
				}
			} catch (FileNotFoundException fnf) {
				
			}
		}

		DbConnection con = null;
		DbResult res = null;
		try {
			con = pool.getConnection();
			DbStatement sth = con.createStatement("SELECT name,path,content,modified FROM " + prefix + "_entry WHERE path = $path$");
			MProperties prop = new MProperties();
			prop.setString("path", path);
			res = sth.executeQuery(prop);
			if (res.next()) {

				// found
				
				InputStream is = res.getBinaryStream("content");
				Date modify = res.getDate("modified");
				String name = res.getString("name");
//				String path2 = res.getString("path");
				id = api.takeFile(is, 0, modify.getTime(), name);
				synchronized (queueCache) {
					queueCache.put(path, id);
				}

				// finally
				if (res.next()) {
					log().w("more then one entry for",path);
				}
				
				return api.getUri(id);

			}
			return null;
		} catch (Exception e) {
			throw new IOException(uri.toString(),e);
		} finally {
			if (res != null) try {res.close();} catch (Throwable t) {log().e(t);};
			if (con != null) con.close();
		}
	}

	@Override
	public Map<String, MUri> getDirectoryList(MUri uri) {
		init();

		TreeMap<String,MUri> out = new TreeMap<>();
		String path = normalizePath(uri.getPath());
		if (!path.endsWith("/")) path = path +"/";
		int pathLevel = MString.countCharacters(path, '/'); //TODO -1 ?
		
		DbConnection con = null;
		DbResult res = null;
		try {
			con = pool.getConnection();
			DbStatement sth = con.createStatement("SELECT name,path,content,modified FROM " + prefix + "_entry WHERE path like $path$ AND pathlevel = $pathlevel$ order by name");
			MProperties prop = new MProperties();
			prop.setString("path", path + "%");
			prop.setInt("pathlevel", pathLevel);
			res = sth.executeQuery(prop);
		} catch (Exception e) {
			log().e(uri,e);
			return out;
		} finally {
			if (res != null) try {res.close();} catch (Throwable t) {log().e(t);};
			if (con != null) con.close();
		}

		return null;
	}

	private String normalizePath(String path) {
		return path.trim().replace('%', '_');
	}

	@Override
	public void importFile(MUri queueUri, MUri target) throws IOException {
		init();

		DbConnection con = null;
		try {
			if (!AaaUtil.isCurrentAdmin())
				throw new IOException("Not supported"); // TODO use ACL!!!!
	
			FileQueueApi api = MApi.lookup(FileQueueApi.class);
			if (api == null) throw new IOException("FileQueueApi not found");
			File fromFile = api.loadFile(queueUri);
	
			String targetPath = normalizePath(target.getPath());
			if (targetPath.endsWith("/"))
				throw new IOException("Target is a directory " + targetPath);
			// test for directory
			String dirPath = MFile.getFileDirectory(targetPath);
			if (dirPath != null) {
				dirPath = dirPath + "/";
				EntryData dirEntry = getEntry(dirPath);
				if (dirEntry == null)
					throw new IOException("Directory not found " + dirPath);
			}
			
			Date now = new Date();
			
			EntryData entry = getEntry(target);
			con = pool.getConnection();
			if (entry != null) {
				// update
				DbStatement sth = con.createStatement("UPDATE " + prefix + "_entry SET modified=$modified$, content=$content$ WHERE path=$path$");
				MProperties prop = new MProperties();
				prop.setString("path", targetPath);
				prop.setDate("modified", now);
				prop.put("content", new FileInputStream(fromFile));
				int res = sth.executeUpdate(prop);
				if (res != 1) {
					throw new IOException("Can't update entry " + target);
				}
				
			} else {
				// create
				String targetName = MFile.getFileName(targetPath);
				int pathLevel = MString.countCharacters(targetPath, '/');
				
				DbStatement sth = con.createStatement("INSERT INTO " + prefix + "_entry (name,path,pathlevel,created,modified,type,content) "
						+ "VALUES ($name$,$path$,$pathlevel$,$created$,$modified$,0,$content$)");
				MProperties prop = new MProperties();
				prop.setString("name", targetName);
				prop.setString("path", targetPath);
				prop.setInt("pathlevel", pathLevel);
				prop.setDate("created", now);
				prop.setDate("modified", now);
				prop.put("content", new FileInputStream(fromFile));
				int res = sth.executeUpdate(prop);
				if (res != 1) {
					throw new IOException("Can't insert entry " + target);
				}
			}
			
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (con != null) con.close();
		}
		
	}

	@Override
	public void deleteFile(MUri uri) throws IOException {
		String path = normalizePath(uri.getPath());
		DbConnection con = null;
		try {
			if (!AaaUtil.isCurrentAdmin())
				throw new IOException("Not supported"); // TODO use ACL!!!!
	
			con = pool.getConnection();
			if (path.endsWith("/")) {
				// is directory
				DbStatement sth = con.createStatement("DELETE FROM " + prefix + "_entry WHERE path like $path$");
				MProperties prop = new MProperties();
				prop.setString("path", path + "%");
				int res = sth.executeUpdate(prop);
				if (res == 0) {
					throw new IOException("File not found: " + path);
				}
			} else {
				// is file
				DbStatement sth = con.createStatement("DELETE FROM " + prefix + "_entry WHERE path = $path$");
				MProperties prop = new MProperties();
				prop.setString("path", path);
				int res = sth.executeUpdate(prop);
				if (res == 0) {
					throw new IOException("File not found: " + path);
				}
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (con != null) con.close();
		}
	}

	@Override
	public void createDirecories(MUri uri) throws IOException {
		String path = normalizePath(uri.getPath());
		DbConnection con = null;
		try {
			if (!AaaUtil.isCurrentAdmin())
				throw new IOException("Not supported"); // TODO use ACL!!!!
	
			con = pool.getConnection();
			DbStatement sth = con.createStatement("INSERT INTO " + prefix + "_entry (name,path,pathlevel,created,modified,type) "
					+ "VALUES ($name$,$path$,$pathlevel$,$created$,$modified$,1)");
			
			while (path.endsWith("/")) path = path.substring(0, path.length()-1);
			if (path.length() == 0) return;
			
			Date now = new Date();
			StringBuilder cur = new StringBuilder().append('/');
			for (String part : path.split("/")) {
				cur.append(part).append('/');
				String curStr = cur.toString();
				EntryData entry = getEntry(curStr);
				if (entry == null) {
					int pathLevel = MString.countCharacters(curStr, '/');
					MProperties prop = new MProperties();
					prop.setString("name", part);
					prop.setString("path", curStr);
					prop.setInt("pathlevel", pathLevel);
					prop.setDate("created", now);
					prop.setDate("modified", now);
					int res = sth.executeUpdate(prop);
					if (res != 1) {
						throw new IOException("Can't insert entry " + curStr);
					}
				}
			}
			
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (con != null) con.close();
		}

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
		parameters.put(PARAM_SCHEME, scheme);
		parameters.put(OperationDescription.TAGS, "acl=" + acl );
	}

	private void init() {
		dataSource = new DataSourceUtil().getDataSource(dataSourceName);
		if (dataSource == null) throw new MRuntimeException("datasource not found", dataSourceName);
		if (dsProvider != null) {
			dsProvider.setDataSource(dataSource);
			return;
		}
		
		
		dsProvider = new DataSourceProvider();
		dsProvider.setDataSource(dataSource);
		pool = new DefaultDbPool(dsProvider);
		try {
			URL url = MSystem.locateResource(this, "SqlDfsStorage.xml");
			DbConnection con = pool.getConnection();
			XmlConfigFile data = new XmlConfigFile(url.openStream());
			data.setString("prefix", prefix);
			pool.getDialect().createStructure(data, con, null, false);
			con.close();
		} catch (Exception e) {
			log().e(e);
		}
	}

	public String getScheme() {
		return scheme;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getAcl() {
		return acl;
	}

	public String getDataSource() {
		return dataSourceName;
	}

	private static class EntryData {

		@SuppressWarnings("unused")
		private String name;
		private long size;
		@SuppressWarnings("unused")
		private String path;
		@SuppressWarnings("unused")
		private Date created;
		private Date modified;

		public EntryData(DbResult res) throws Exception {
			this.name = res.getString("name");
			this.size = res.getLong("size");
			this.path = res.getString("path");
			this.created = res.getDate("created");
			this.modified = res.getDate("modified");
		}
		
	}

}
