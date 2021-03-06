/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.sop.api.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mark a method to be used as rest action use the following method pattern
 *
 * <p>public void onAction(JsonResult result, CallContext callContext) throws Exception
 *
 * @author mikehummel
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RestAction {

    String name();

    String contentType() default "text/plain";
}
