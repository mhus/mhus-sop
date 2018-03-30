package de.mhus.osgi.sop.vaadin.desktop;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.security.AccessControl;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.MSecurity;
import de.mhus.lib.vaadin.VaadinAccessControl;
import de.mhus.lib.vaadin.desktop.Desktop;
import de.mhus.lib.vaadin.desktop.GuiSpace;
import de.mhus.lib.vaadin.desktop.GuiSpaceService;
import de.mhus.lib.vaadin.login.LoginScreen;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

@Theme("soptheme")
@Widgetset("de.mhus.osgi.sop.vaadin.theme.SopWidgetset")
public class SopUi extends UI implements SopUiApi {
	
	// https://ccsearch.creativecommons.org/image/detail/pugDQPO07WYrRd52PHD68Q==
	// "cross process=loves" by Vivianna_love is licensed under CC BY 2.0
	
	private static final long serialVersionUID = 1L;
	
//	private static CfgString CFG_REALM = new CfgString(SopUi.class, "realm", "karaf");
	private static Log log = Log.getLog(SopUi.class);
	private Desktop desktop;
	private AccessControl accessControl;
	private ServiceTracker<GuiSpaceService,GuiSpaceService> spaceTracker;
	private BundleContext context;
	private String trailConfig = null;
	
	private String startNav;
	private String host;
	
	@Override
	protected void init(VaadinRequest request) {
		
		startNav = UI.getCurrent().getPage().getUriFragment();

        desktop = new Desktop(this) {
			private static final long serialVersionUID = 1L;
			private MenuItem menuTrace;
			private Refresher refresher;
        	
        	@SuppressWarnings("deprecation")
			@Override
			protected void initGui() {
        		super.initGui();
        		
        		String part = UI.getCurrent().getPage().getUriFragment();
        		
        		Date now = new Date();
        		if ("easter".equals(part) || now.getMonth() == 3 && (now.getDate() >= 10 && now.getDate() <= 17 ))
        			addStyleName("desktop-easter");
        		if ("towel".equals(part) || now.getMonth() == 4 && now.getDate() == 25)
        			addStyleName("desktop-towel");
        		if ("yoda".equals(part) || now.getMonth() == 4 && now.getDate() == 21)
        			addStyleName("desktop-yoda");
        		if ("pirate".equals(part) || now.getMonth() == 8 && now.getDate() == 19)
        			addStyleName("desktop-pirate");
        		if ("suit".equals(part) || now.getMonth() == 9 && now.getDate() == 13)
        			addStyleName("desktop-suit");
        		
        		refresher = new Refresher();
        		refresher.setRefreshInterval(1000);
        		refresher.addListener(new Refresher.RefreshListener() {
					private static final long serialVersionUID = 1L;
					@Override
        			public void refresh(Refresher source) {
        				doTick();
        			}
        		});
        		addExtension(refresher);

        		menuTrace = menuUser.addItem("Trace An", new MenuBar.Command() {
					private static final long serialVersionUID = 1L;
					@Override
        			public void menuSelected(MenuItem selectedItem) {
        				if (getTrailConfig() == null) {
        					setTrailConfig("MAP");
        					menuTrace.setText("Trace Aus (" + MLogUtil.getTrailConfig() + ")");
        				} else {
        					setTrailConfig(null);
        					menuTrace.setText("Trace An");
        				}
        			}
        		});
        		
        	}
        	
        	protected void doTick() {
        		
        	}
        };
        
        VerticalLayout content = new VerticalLayout();
        setContent(content);
        content.setSizeFull();
        content.addStyleName("view-content");
        content.setMargin(true);
        content.setSpacing(true);
        
        context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        spaceTracker = new ServiceTracker<>(context, GuiSpaceService.class, new GuiSpaceServiceTrackerCustomizer() );
        spaceTracker.open();
        
        host = request.getHeader("Host");
        
//        accessControl = new VaadinAccessControl(CFG_REALM.value());
        accessControl = new VaadinSopAccessControl();

        if (!accessControl.isUserSignedIn()) {
            setContent(new LoginScreen(accessControl, new LoginScreen.LoginListener() {
				private static final long serialVersionUID = 1L;
				@Override
                public void loginSuccessful() {
                    showMainView();
                }
            }) {
				private static final long serialVersionUID = 1L;
			   } );
        } else {
            showMainView();
        }
	}

	private void showMainView() {
        addStyleName(ValoTheme.UI_WITH_MENU);
        setContent(desktop);
		desktop.refreshSpaceList();
		
		if (MString.isSet(startNav)) {
			
			if (startNav.startsWith("!")) startNav=startNav.substring(1);
			
			if (MString.isIndex(startNav, ':')) {
				String backLink = MString.beforeIndex(startNav, ':');
				startNav = MString.afterIndex(startNav, ':');
				if (MString.isSet(backLink))
					desktop.rememberNavigation("Webseite", "site", "", backLink, false);
			}
			
			String[] parts = startNav.split("/", 3);
			if (parts.length > 0) {
				String space = parts[0];
				String subSpace = parts.length > 1 ? parts[1] : null;
				String filter = parts.length > 2 ? parts[2] : null;
				
				desktop.openSpace(space, subSpace, filter, false, false);

			}
			
			startNav = null;
		}
	}

	@Override
	public void close() {
		synchronized (this) {
			spaceTracker.close();
			desktop.close();
		}
		super.close();
	}

	private class GuiSpaceServiceTrackerCustomizer implements ServiceTrackerCustomizer<GuiSpaceService,GuiSpaceService> {

		@Override
		public GuiSpaceService addingService(
				ServiceReference<GuiSpaceService> reference) {
			synchronized (this) {
				GuiSpaceService service = context.getService(reference);
				desktop.addSpace(service);
				return service;
			}
		}

		@Override
		public void modifiedService(
				ServiceReference<GuiSpaceService> reference,
				GuiSpaceService service) {
			synchronized (this) {
				desktop.removeSpace(service);
				service = context.getService(reference);
				desktop.addSpace(service);
			}
		}

		@Override
		public void removedService(ServiceReference<GuiSpaceService> reference,
				GuiSpaceService service) {
			synchronized (this) {
				desktop.removeSpace(service);
			}
		}
	}

	@Override
	public BundleContext getContext() {
		return context;
	}
	
	@Override
	public AccessControl getAccessControl() {
		return accessControl;
	}
	
	@Override
	public boolean openSpace(String spaceId, String subSpace, String search) {
		return desktop.openSpace(spaceId, subSpace, search);
	}
	
	@Override
	public boolean openSpace(String spaceId, String subSpace, String search, boolean history, boolean navLink) {
		return desktop.openSpace(spaceId, subSpace, search, history, navLink);
	}
	
	@Override
	public void rememberNavigation(GuiSpace space, String subSpace, String search, boolean navLink) {
		desktop.rememberNavigation(GuiUtil.getHistoryCaption(space.getDisplayName(Locale.GERMAN), subSpace, search), space.getName(), subSpace, search, navLink);
	}
	
	@Override
	public void rememberNavigation(String caption, String space, String subSpace, String search, boolean navLink) {
		desktop.rememberNavigation(caption, space, subSpace, search, navLink);
	}

	@Override
	public boolean hasAccess(String role) {
		if (role == null || accessControl == null || !accessControl.isUserSignedIn())
			return false;

		try {
			File file = new File("sop/aaa/groupmapping/" + MFile.normalize(role.trim()).toLowerCase() + ".txt"); 
			if (!file.exists()) {
				log.w("file not found",file);
				return false;
			}
			List<String> lines = MFile.readLines(file, true);
			for (String line : lines) {
				line = MString.beforeLastIndex(line, '|'); // remove additional rights to only evaluate general access
				if (line.startsWith("not:")) {
					line = line.substring(4);
					if (accessControl.hasGroup(line)) return false;
				} else
				if (line.startsWith("notuser:")) {
					line = line.substring(8);
					if (accessControl.getName().equals(line)) return false;
				} else
				if (line.startsWith("user:")) {
					line = line.substring(5);
					if (accessControl.getName().equals(line)) return true;
				} else
				if (line.equals("*") || accessControl.hasGroup(line)) return true;
			}
		} catch (Throwable t) {
			log.d(role,t);
		}
		return false;
		
	}
	
	@Override
	public boolean hasWriteAccess(String role) {
		if (role == null || accessControl == null || !accessControl.isUserSignedIn())
			return false;

		try {
			File file = new File("sop/aaa/groupmapping/" + MFile.normalize(role.trim()).toLowerCase() + ".txt"); 
			if (!file.exists()) {
				log.w("file not found",file);
				return false;
			}
			List<String> lines = MFile.readLines(file, true);
			for (String line : lines) {
				// read additional rights
				HashSet<String> rights = new HashSet<>();
				if (line.contains("|")) {
					String[] parts = MString.splitIgnoreEmpty(line, "|", true);
					if (parts.length > 1) {
						line = parts[0];
						for (int i = 1; i < parts.length; i++) {
							rights.add(parts[i]);
						}
					}
				}
				if (line.startsWith("not:")) {
					line = line.substring(4);
					if (accessControl.hasGroup(line)) return false;
				} else
				if (line.startsWith("notuser:")) {
					line = line.substring(8);
					if (accessControl.getName().equals(line)) return false;
				} else
				if (line.startsWith("user:")) {
					line = line.substring(5);
					if (accessControl.getName().equals(line)) {
						if (rights.contains("read"))
							return false;
						return true;
					}
				} else
				if (line.equals("*") || (accessControl.hasGroup(line))) {
					if (rights.contains("read"))
						return false;
					return true;
				}
			}
		} catch (Throwable t) {
			log.d(role,t);
		}
		return false;
		
	}
	

	@Override
	public IProperties getCurrentUserAccess() {
		MProperties accessRights = new MProperties();
		if (accessControl == null || !accessControl.isUserSignedIn())
			return accessRights;
		
		try {
			File file = new File("sop/aaa/guiusers/" + MFile.normalize(accessControl.getName().trim()).toLowerCase() + ".txt"); 
			if (!file.exists()) {
				log.w("file not found",file);
				return accessRights;
			}
			List<String> lines = MFile.readLines(file, true);
			if (lines == null)
				return new MProperties();
			
			if (lines.size() == 1 && "*".equals(lines.get(0)) || "admin".equalsIgnoreCase(lines.get(0))) {
				accessRights.setString("read", "*");
				accessRights.setString("write", "*");
			} else {
				accessRights = MProperties.load(file.getAbsolutePath());
			}
		} catch (Throwable t) {
			log.d(t);
		}
		
		return accessRights;
	}

	public Account getCurrentUser() {
		return VaadinSopAccessControl.getUserAccount(getSession());
	}

	@Override
	public String getCurrentUserName() {
		return VaadinSopAccessControl.getUserName(getSession());
	}

	public void requestBegin() {
		if (trailConfig != null)
			MLogUtil.setTrailConfig(trailConfig);
		else
			MLogUtil.releaseTrailConfig();
		AccessApi aaa = MApi.lookup(AccessApi.class);
		AaaContext acontext = aaa.processUserSession(getCurrentUserName(), Locale.getDefault());
		getSession().setAttribute("_aaacontext", acontext);
	}

	public void requestEnd() {
		AccessApi aaa = MApi.lookup(AccessApi.class);
		
//		AaaContext acontext = (AaaContext) getSession().getAttribute("_aaacontext");
		aaa.resetContext();
		
		MLogUtil.releaseTrailConfig();
	}

	public String getTrailConfig() {
		return trailConfig;
	}

	public void setTrailConfig(String trailConfig) {
		if (trailConfig == null) {
			this.trailConfig = trailConfig;
			MLogUtil.releaseTrailConfig();
		} else {
			MLogUtil.setTrailConfig(trailConfig);
			this.trailConfig = MLogUtil.getTrailConfig();
		}
	}
	
	@Override
	public String getHost() {
		return host;
	}
	
}
