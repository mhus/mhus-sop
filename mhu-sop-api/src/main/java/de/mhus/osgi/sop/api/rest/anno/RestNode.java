package de.mhus.osgi.sop.api.rest.anno;

import de.mhus.osgi.sop.api.rest.RestNodeService;

public @interface RestNode {
	String name();

	String acl() default "*";

	String parent() default RestNodeService.ROOT_ID;
}
