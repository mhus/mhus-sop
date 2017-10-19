package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

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
}
