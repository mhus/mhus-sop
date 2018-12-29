package de.mhus.osgi.sop.api.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestAction {

	String name();

	String contentType() default "text/plain";

}
