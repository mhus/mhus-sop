package de.mhus.osgi.sop.api.util;

import java.io.File;

public class SopUtil {
	
	private static File base = new File("sop");
	
	public static File getFile(String path) {
		return new File(base, path);
	}

}
