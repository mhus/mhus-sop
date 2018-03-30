package de.mhus.osgi.sop.vaadin.desktop;

import com.vaadin.ui.UI;

import de.mhus.lib.core.MString;
import de.mhus.lib.vaadin.desktop.GuiApi;

public class GuiUtil {

	public static GuiApi getApi() {
		return (GuiApi) UI.getCurrent();
	}
	
	public static String getHistoryCaption(String spaceDisplayName, String subSpaceDisplayName, String filter) {
		return spaceDisplayName + (MString.isSet(subSpaceDisplayName) ? '/' + subSpaceDisplayName : "") + (MString.isSet(filter) ? " [" + filter + ']' : "");
	}

}
