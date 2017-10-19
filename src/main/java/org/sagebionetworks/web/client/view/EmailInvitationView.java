package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EmailInvitationView extends IsWidget {
	void setInvitationTitle(String title);

	void setInvitationMessage(String message);

	void setRegisterWidget(Widget w);

	void setSynapseAlertContainer(Widget w);

	void setPresenter(Presenter presenter);

	void showInfo(String title, String message);

	void clear();

	void show();

	interface Presenter {
		void onLoginClick();
	}
}