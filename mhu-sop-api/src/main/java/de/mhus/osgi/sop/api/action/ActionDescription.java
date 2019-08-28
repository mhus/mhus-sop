package de.mhus.osgi.sop.api.action;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface ActionDescription {

    /**
     * Name of the processed queue
     * @return Queue name
     */
    String queue();

    /**
     * Limit of processed actions in one round (scheduler event).
     * @return Number of maximal actions to process
     */
    int limit() default 0;

    /**
     * Time to wait after modify changed to process the action. So processing can be scheduled after creation and
     * re processing.
     * 
     * @return Time interval to last modify date
     */
    long timeToWait() default 0;
}
