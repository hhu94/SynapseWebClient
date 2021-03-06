package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface TeamBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setTeam(Team team, Integer maxNameLength, ClickHandler customClickHandler);

	public void showLoadError(String principalId);
	
	public void setRequestCount(String count);
	
	void setTeamWithoutLink(String name, String teamId);

	void setVisible(boolean visible);
	void addStyleName(String style);
	void setTarget(String target);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
