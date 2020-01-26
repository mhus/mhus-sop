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

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
        service = Servlet.class,
        property = "alias=/sop",
        name = "SopDesktop",
        servicefactory = true)
@VaadinServletConfiguration(ui = SopUi.class, productionMode = true)
public class SopServlet extends VaadinServlet {

    private static final long serialVersionUID = 1L;
    private BundleContext context;

    @Activate
    public void activate(ComponentContext ctx) {
        this.context = ctx.getBundleContext();
    }

    public BundleContext getBundleContext() {
        return context;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            super.service(request, response);
        } finally {
            // cleanup
            try {
                VaadinSession vaadinSession = (VaadinSession) request.getAttribute("__vs");
                if (vaadinSession != null) {
                    for (UI ui : vaadinSession.getUIs()) {
                        if (ui instanceof SopUi) ((SopUi) ui).requestEnd();
                    }
                }
            } catch (Throwable t) {

            }
        }
    }

    @Override
    protected boolean isStaticResourceRequest(HttpServletRequest request) {
        // set user and trace ...

        boolean ret = super.isStaticResourceRequest(request);
        if (!ret) {
            try {
                VaadinServletRequest vs = createVaadinRequest(request);
                VaadinSession vaadinSession = getService().findVaadinSession(vs);
                request.setAttribute("__vs", vaadinSession);
                for (UI ui : vaadinSession.getUIs()) {
                    if (ui instanceof SopUi) ((SopUi) ui).requestBegin();
                }
            } catch (Throwable t) {

            }
        }
        return ret;
    }
}
