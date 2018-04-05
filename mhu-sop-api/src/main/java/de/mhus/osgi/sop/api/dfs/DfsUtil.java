package de.mhus.osgi.sop.api.dfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;

public class DfsUtil {

	public static String provideClassPathResource(ClassLoader loader, String path) throws IOException {
		InputStream is = loader.getResourceAsStream(path);
		String ret = provideResource(is, MFile.getFileName(path));
		is.close();
		return ret;
	}

	public static String provideResource(InputStream is, String name) throws IOException {
		FileQueueApi api = MApi.lookup(FileQueueApi.class);
		UUID id = api.createQueueFile("", FileQueueApi.DEFAULT_TTL);
		OutputStream os = api.createQueueFileOutputStream(id);
		MFile.copyFile(is, os);
		os.close();
		return api.getUri(id).toString();
	}
	
}
