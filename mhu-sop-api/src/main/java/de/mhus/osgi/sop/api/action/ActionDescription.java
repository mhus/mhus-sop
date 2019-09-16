/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
