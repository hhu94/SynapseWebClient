package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	@UiField
	Image synapseLogo;
	@UiField
	Row headerDiv;
	@UiField
	Anchor projectHeadingAnchor;
	@UiField
	Div headingPanel;

	@UiField
	Button dashboardButton;
	@UiField
	ButtonGroup headerFavButtonGroup;
	@UiField
	Button headerFavButton;
	@UiField
	DropDownMenu headerFavList;

	@UiField
	SimplePanel projectFavoritePanel;
	@UiField
	SimplePanel stuAnnouncementsContainer;
	@UiField
	SimplePanel registerLinkUI;
	@UiField
	SimplePanel dashboardButtonUI;
	@UiField
	Button registerLink;
	@UiField
	SimplePanel loginLinkUI;
	@UiField
	Button loginLink;
	
	@UiField
	Span headerButtons;
	
	@UiField
	Anchor trashLink;
	@UiField
	Button logoutLink;

	@UiField
	FlowPanel testSitePanel;
	@UiField
	Anchor goToStandardSite;
	@UiField
	SimplePanel searchBoxContainer;
	@UiField
	Alert stagingAlert;
	@UiField
	Button helpButton;
	
	private Presenter presenter;
	private SearchBox searchBox;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	private GlobalApplicationState globalAppState;
	UserBadge userBadge;
	Span userBadgeText;
	HorizontalPanel myDashboardButtonContents;
	@Inject
	public HeaderViewImpl(Binder binder,
			SageImageBundle sageImageBundle,
			SearchBox searchBox,
			CookieProvider cookies,
			UserBadge userBadge,
			GlobalApplicationState globalAppState) {
		this.initWidget(binder.createAndBindUi(this));
		this.searchBox = searchBox;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		this.userBadge = userBadge;
		this.globalAppState = globalAppState;
		userBadge.setSize(BadgeSize.SMALL_PICTURE_ONLY);
		// add search panel first
		searchBox.setVisible(true);
		searchBoxContainer.setWidget(searchBox.asWidget());
		myDashboardButtonContents = new HorizontalPanel();
		myDashboardButtonContents.addStyleName("moveup-1");
		myDashboardButtonContents.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		myDashboardButtonContents.add(userBadge.asWidget());
		userBadgeText = new Span();
		myDashboardButtonContents.add(userBadgeText);
		addUserPicturePanel();
		initClickHandlers();
		refreshTestSiteHeader();
		clear();
	}
	
	@Override
	public void clear() {
		setProjectHeaderText("");
		showSmallLogo();
	}
	
	@Override
	public void setProjectHeaderText(String text) {
		projectHeadingAnchor.setText(text);
	}
	
	@Override
	public void setProjectHeaderAnchorTarget(String href) {
		projectHeadingAnchor.setHref(href);
	}
	
	@Override
	public void setProjectFavoriteWidget(IsWidget favWidget) {
		projectFavoritePanel.setWidget(favWidget);
	}
	
	@Override
	public void showProjectFavoriteWidget() {
		projectFavoritePanel.setVisible(true);
	}
	
	@Override
	public void hideProjectFavoriteWidget() {
		projectFavoritePanel.setVisible(false);
	}
	
	@Override
	public void showLargeLogo() {
		projectHeadingAnchor.addStyleName("font-size-67");
		synapseLogo.removeStyleName("margin-bottom-15");
		synapseLogo.addStyleName("margin-bottom-40");
		synapseLogo.setHeight("66px");
		synapseLogo.setWidth("66px");
		headerDiv.setPaddingTop(16);
		headerButtons.setMarginTop(28);
	}
	
	@Override
	public void showSmallLogo() {
		projectHeadingAnchor.removeStyleName("font-size-67");
		synapseLogo.removeStyleName("margin-bottom-40");
		synapseLogo.addStyleName("margin-bottom-15");
		synapseLogo.setHeight("25px");
		synapseLogo.setWidth("25px");
		headerDiv.setPaddingTop(9);
		headerButtons.setMarginTop(0);
	}
	
	/**
	 * Clear the divider/caret from the user button, and add the picture container
	 * @param button
	 */
	public void addUserPicturePanel() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
            	dashboardButton.add(myDashboardButtonContents);
            }
        });
	}

	public void initClickHandlers() {
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.setTestWebsite(false, cookies);
				globalAppState.refreshPage();
			}
		});
		trashLink.addClickHandler(new ClickHandler() {
    		@Override
			public void onClick(ClickEvent event) {
    			presenter.onTrashClick();
			}
    	});
		logoutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLogoutClick();
			}
		});
		dashboardButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDashboardClick();
			}
		});
		loginLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLoginClick();
			}
		});
		registerLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRegisterClick();
			}
		});
		
		headerFavButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFavoriteClick();
			}
		});
		synapseLogo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLogoClick();
			}
		});
		helpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(WebConstants.DOCS_BASE_URL, "", "");
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		refresh();
	}

	@Override
	public void setMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void removeMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void refresh() {
		refreshTestSiteHeader();
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
		trashLink.setVisible(isInTestWebsite);
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBox.setVisible(searchVisible);
	}

	@Override
	public void openNewWindow(String url) {
		DisplayUtils.newWindow(url, "", "");
	}
	
	@Override
	public void setUser(UserSessionData userData) {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
	 	trashLink.setVisible(isInTestWebsite);
	 	userBadge.clearState();
	 	userBadge.setDoNothingOnClick();
	 	if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
	 		userBadge.configure(userData.getProfile());
	 		userBadgeText.setText(DisplayUtils.getDisplayName(userData.getProfile()));
			loginLinkUI.setVisible(false);
			registerLinkUI.setVisible(false);
			logoutLink.setVisible(true);
			dashboardButtonUI.setVisible(true);
			headerFavButtonGroup.setVisible(true);
		} else {
			loginLinkUI.setVisible(true);
			registerLinkUI.setVisible(true);
			logoutLink.setVisible(false);
			dashboardButtonUI.setVisible(false);
			headerFavButtonGroup.setVisible(false);
		}
	}

	@Override
	public void clearFavorite() {
		headerFavList.clear();
	}

	@Override
	public void setEmptyFavorite() {
		AnchorListItem defaultItem = new AnchorListItem("Empty");
		headerFavList.add(defaultItem);
	}

	@Override
	public void addFavorite(List<EntityHeader> headers) {
		for (final EntityHeader header : headers) {
			AnchorListItem favItem = new AnchorListItem(header.getName());
			favItem.setHref(DisplayUtils.getSynapseHistoryToken(header.getId()));
			headerFavList.add(favItem);
		}
	}

	@Override
	public void setStagingAlertVisible(boolean visible) {
		stagingAlert.setVisible(visible);	
	}
	
	/*
	 * Private Methods
	 */

	private void refreshTestSiteHeader() {
		testSitePanel.setVisible(DisplayUtils.isInTestWebsite(cookies));
	}
	
	@Override
	public void showFavoritesLoading() {
		headerFavList.clear();
		AnchorListItem loading = new AnchorListItem("Loading...");
		loading.setEnabled(false);
		headerFavList.add(loading);
	}
	@Override
	public void setStuAnnouncementWidget(Widget w) {
		stuAnnouncementsContainer.clear();
		stuAnnouncementsContainer.add(w);
	}
}
