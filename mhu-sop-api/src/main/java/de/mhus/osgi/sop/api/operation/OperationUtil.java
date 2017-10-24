package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.VersionRange;

public class OperationUtil {

	public static boolean matches(OperationDescriptor desc, String filter, VersionRange version, Collection<String> providedTags) {
		return  (filter == null || MString.compareFsLikePattern(desc.getPath(), filter)) 
				&& 
				(version == null || version.includes(desc.getVersion())) 
				&& 
				(providedTags == null || desc.compareTags(providedTags));
	}
	
	public static boolean isOption(String[] options, String opt) {
		if (options == null || opt == null) return false;
		for (String o : options)
			if (opt.equals(o)) return true;
		return false;
	}

	public static int getOption(String[] options, String opt, int def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return MCast.toint(o.substring(o.length()), def);
		return def;
	}

	public static long getOption(String[] options, String opt, long def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return MCast.tolong(o.substring(o.length()), def);
		return def;
	}

	public static String getOption(String[] options, String opt, String def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return o.substring(o.length());
		return def;
	}

}
