package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface OpenUserInvitationsWidgetView extends IsWidget {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void setSynAlert(IsWidget w);
	void clear();
	/**
	 * shows nothing if membershipRequests is empty.
	 */
	public void configure(List<UserProfile> profiles, List<MembershipInvtnSubmission> invitations);
	
	public void setMoreResultsVisible(boolean isVisible);
	
	public interface Presenter {
		//use to go to user profile page
		void goTo(Place place);
		void removeInvitation(String ownerId);
		void getNextBatch();
	}
}
