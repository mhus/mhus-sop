package de.mhus.osgi.sop.vaadin.desktop;

import org.osgi.framework.BundleContext;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.vaadin.desktop.GuiApi;
import de.mhus.lib.vaadin.desktop.GuiSpace;

public interface SopUiApi extends GuiApi {

	boolean openSpace(String spaceId, String subSpace, String search);

	boolean openSpace(String spaceId, String subSpace, String search, boolean history, boolean navLink);

	void rememberNavigation(GuiSpace space, String subSpace, String search, boolean navLink);

	void rememberNavigation(String caption, String space, String subSpace, String search, boolean navLink);

	boolean hasWriteAccess(String role);

	IProperties getCurrentUserAccess();

	BundleContext getContext();

}
