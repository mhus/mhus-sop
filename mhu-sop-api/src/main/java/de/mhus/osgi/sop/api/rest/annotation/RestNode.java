package de.mhus.osgi.sop.api.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.mhus.osgi.sop.api.rest.RestNodeService;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestNode {
	String name();

	String acl() default "*";

	String[] parent() default RestNodeService.ROOT_ID;
}
