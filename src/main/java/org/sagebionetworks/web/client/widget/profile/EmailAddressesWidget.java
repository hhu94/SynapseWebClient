package org.sagebionetworks.web.client.widget.profile;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/** 
 * @author Jay
 *
 */
public class EmailAddressesWidget implements EmailAddressesWidgetView.Presenter, IsWidget {
	EmailAddressesWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	AuthenticationController authenticationController;
	PopupUtilsView popupUtils;
	UserProfile profile;
	GWTWrapper gwt;
	AsyncCallback<Void> refreshCallback;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public EmailAddressesWidget(EmailAddressesWidgetView view, 
			SynapseClientAsync synapseClient,
			SynapseJavascriptClient jsClient,
			SynapseAlert synAlert,
			AuthenticationController authenticationController,
			PopupUtilsView popupUtils,
			GWTWrapper gwt) {
		this.synapseClient = synapseClient;
		this.jsClient = jsClient;
		this.view = view;
		this.synAlert = synAlert;
		this.authenticationController = authenticationController;
		this.popupUtils = popupUtils;
		this.gwt = gwt;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		refreshCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				refresh();
			}
			@Override
			public void onFailure(Throwable caught) {
				EmailAddressesWidget.this.synAlert.handleException(caught);
			}
		};
	}

	public void clear() {
		synAlert.clear();
		view.clearEmails();
	}
	public void configure(final UserProfile profile) {
		this.profile = profile;
		clear();
		if (authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(profile.getOwnerId())) {
			view.setLoadingVisible(true);
			synapseClient.getNotificationEmail(new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					view.setLoadingVisible(false);
					synAlert.handleException(caught);
				}
				@Override
				public void onSuccess(String notificationEmail) {
					view.setLoadingVisible(false);
					view.addPrimaryEmail(notificationEmail);
					for (String email : profile.getEmails()) {
						if (!notificationEmail.equals(email)) {
							view.addSecondaryEmail(email);
						}
					}
				}
			});
			view.setVisible(true);
		} else {
			view.setVisible(false);
		}
	}

	public void refresh() {
		clear();
		view.setLoadingVisible(true);
		jsClient.getUserProfile(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile result) {
				configure(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onAddEmail(String emailAddress) {
		emailAddress = emailAddress.trim();
		// Is this email already in the profile email list?
		// If so, just update it as the new notification email. Otherwise, kick
		// off the verification process.
		List<String> emailAddresses = profile.getEmails();
		if (emailAddresses == null || emailAddresses.isEmpty())
			throw new IllegalStateException("UserProfile email list is empty");
		for (String email : emailAddresses) {
			if (email.equalsIgnoreCase(emailAddress)) {
				// update the notification email
				onMakePrimary(emailAddress);
				return;
			}
		}
		// did not find in the list
		additionalEmailValidation(emailAddress);
	}

	public void additionalEmailValidation(String emailAddress) {
		// need to validate
		//first, does it look like an email address?
		emailAddress = emailAddress.trim();
		if (!ValidationUtils.isValidEmail(emailAddress)) {
			synAlert.showError(WebConstants.INVALID_EMAIL_MESSAGE);
			return;
		}

		String callbackUrl = gwt.getHostPageBaseURL() + "#!Account:";
		synapseClient.additionalEmailValidation(
				authenticationController.getCurrentUserPrincipalId(),
				emailAddress, callbackUrl, new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						popupUtils.showInfo(DisplayConstants.EMAIL_ADDED, "");
					}

					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}
				});
	}
	
	@Override
	public void onMakePrimary(String email) {
		synAlert.clear();
		synapseClient.setNotificationEmail(email, refreshCallback);
	}
	
	@Override
	public void onRemoveEmail(String email) {
		synAlert.clear();
		synapseClient.removeEmail(email.trim(), refreshCallback);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
