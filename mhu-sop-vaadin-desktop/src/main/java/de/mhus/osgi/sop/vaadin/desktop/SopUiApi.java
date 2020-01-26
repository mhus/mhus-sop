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
package de.mhus.osgi.sop.vaadin.desktop;

import org.osgi.framework.BundleContext;

import de.mhus.lib.vaadin.desktop.GuiApi;
import de.mhus.lib.vaadin.desktop.GuiSpace;

public interface SopUiApi extends GuiApi {

    boolean openSpace(String spaceId, String subSpace, String search);

    boolean openSpace(
            String spaceId, String subSpace, String search, boolean history, boolean navLink);

    void rememberNavigation(GuiSpace space, String subSpace, String search, boolean navLink);

    void rememberNavigation(
            String caption, String space, String subSpace, String search, boolean navLink);

    boolean hasWriteAccess(String role);

    BundleContext getContext();
}
