package org.sagebionetworks.web.client.widget.header;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.DatasetsHome;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.users.UserData;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HeaderViewImpl extends Composite implements HeaderView {

	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	@UiField
	HorizontalPanel commandBar;
	
	@UiField
	SimplePanel searchBoxPanel;
		
	private Presenter presenter;
	private Map<MenuItems, Element> itemToElement;
	private AuthenticationController authenticationController;	
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private LayoutContainer jumpTo;
	private TextField<String> jumpToField;
	private Button goButton;
	private SearchBox searchBox;	
	private Button userButton;
	private Button loginButton;
	private Button registerButton;
	
	@Inject
	public HeaderViewImpl(Binder binder, AuthenticationControllerImpl authenticationController, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, GlobalApplicationState globalApplicationState, SearchBox searchBox) {
		this.initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
		
		// add search panel
		searchBoxPanel.clear();		
		searchBoxPanel.add(searchBox.asWidget());
		searchBoxPanel.setVisible(false);
		commandBar.addStyleName("sf-j-menu");
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		setUser(presenter.getUser());		
	}

	@Override
	public void setMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void removeMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void refresh() {
		setUser(presenter.getUser());		
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBoxPanel.setVisible(searchVisible);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void setUser(UserData userData) {
		//initialize buttons
		if(userButton == null) {
			userButton = new Button();
			userButton.setId(DisplayConstants.ID_BTN_USER);
			userButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.user16()));
			userButton.setHeight(35);
			configureUserMenu();
		}
		if(loginButton == null) {
			loginButton = new Button(DisplayConstants.BUTTON_LOGIN);
			loginButton.setId(DisplayConstants.ID_BTN_LOGIN);
			loginButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
				}
			});
		}
		if (registerButton == null)
		{
			AbstractImagePrototype icon = null;
			registerButton = new Button(DisplayConstants.BUTTON_REGISTER);
			registerButton.setId(DisplayConstants.ID_BTN_REGISTER);
			registerButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(DisplayUtils.DEFAULT_PLACE_TOKEN));
				}
			});
		}
		
		if(userData != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			userButton.setText(userData.getUserName());
			commandBar.remove(loginButton);
			commandBar.remove(registerButton);
			if (commandBar.getWidgetIndex(userButton) == -1)
				commandBar.add(userButton);
		} else {
			//no user data, add register and login
			commandBar.remove(userButton);
			if (commandBar.getWidgetIndex(registerButton) == -1)
			{
				commandBar.add(registerButton);			
				commandBar.add(loginButton);
			}
		}
	}

	private void configureUserMenu() {				
		// create drop down menu
		Menu menu = new Menu();
//		addMenuItem(DisplayConstants.TEXT_USER_SETTINGS,
//				AbstractImagePrototype.create(iconsImageBundle.cog16()),
//				new Profile(DisplayUtils.DEFAULT_PLACE_TOKEN),
//				menu);
		
		addMenuItem(DisplayConstants.TEXT_USER_VIEW_PROFILE,
				AbstractImagePrototype.create(iconsImageBundle.user16()),
				new Profile(DisplayUtils.DEFAULT_PLACE_TOKEN),
				DisplayConstants.ID_MNU_USER_PROFILE,
				menu);
		
		addMenuItem(DisplayConstants.BUTTON_LOGOUT,
				new LoginPlace(LoginPlace.LOGOUT_TOKEN),
				DisplayConstants.ID_MNU_USER_LOGOUT,
				menu);
		
		userButton.setMenu(menu);
	}
	
	private void addMenuItem(String text, final Place place, String id, Menu menu)
	{
		addMenuItem(text, null, place, id, menu);
	}
	
	private void addMenuItem(String text, AbstractImagePrototype icon, final Place place, String id, Menu menu)
	{
		MenuItem menuItem = new MenuItem(text, icon);
		menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				globalApplicationState.getPlaceChanger().goTo(place);
			}
		});
		menuItem.setId(id);
		menu.add(menuItem);
	}
}


