package de.mhus.osgi.sop.api.dfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;

public class DfsUtil {

	/**
	 * Copy a resource from class path into a dfq managed file. The file is already stored an can be used.
	 * Don't forget to touch the file if you need more time.
	 * 
	 * @param loader Class loader to load resource from or null for default thread class loader
	 * @param path The path to the resource
	 * @return The URI of the dfq resource
	 * @throws IOException
	 */
	public static String provideClassPathResource(ClassLoader loader, String path) throws IOException {
		if (loader == null) loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream(path);
		String ret = provideResource(is, MFile.getFileName(path));
		is.close();
		return ret;
	}

	/**
	 * Copy a resource into a dfq managed file. The file is already stored an can be used.
	 * Don't forget to touch the file if you need more time.
	 * 
	 * @param is Input Stream, data source, the is will not be closed after loading all data.
	 * @param name The name of the file
	 * @return The URI of the dfq resource
	 * @throws IOException
	 */
	public static String provideResource(InputStream is, String name) throws IOException {
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		UUID id = api.createQueueFile("", FileQueueApi.DEFAULT_TTL);
		OutputStream os = api.createQueueFileOutputStream(id);
		MFile.copyFile(is, os);
		os.close();
		return api.getUri(id).toString();
	}
	
	/**
	 * Create a dfq managed file as temp file. The file is stored and writable.
	 * The file is only available in the ttl time. Don't forget to touch the file
	 * if you need more time.
	 * 
	 * @param name A description of the usage for debug purpose
	 * @param ttl The ttl in ms or 0 for default
	 * @return The tmp file uri
	 * @throws IOException
	 */
	public static String createTmpFile(String name, long ttl) throws IOException {
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		UUID id = api.createQueueFile("tmp_" + name, ttl);
		api.closeQueueFile(id);
		File file = api.loadFile(id);
		file.setWritable(true, true);
		return api.getUri(id).toString();
	}
	
}
