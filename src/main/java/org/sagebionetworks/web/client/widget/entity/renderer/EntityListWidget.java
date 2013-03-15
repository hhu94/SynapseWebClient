package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil.RowLoadedHandler;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidget implements EntityListWidgetView.Presenter, WidgetRendererPresenter {
	
	private EntityListWidgetView view;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private Map<String, String> descriptor;
	
	@Inject
	public EntityListWidget(EntityListWidgetView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,  Map<String, String> widgetDescriptor) {
		if (widgetDescriptor == null) throw new IllegalArgumentException("Descriptor can not be null");
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		final boolean isLoggedIn = authenticationController.isLoggedIn();
		
		view.configure();

		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		if(records != null) {
			for(int i=0; i<records.size(); i++) {
				final int rowIndex = i;
				EntityListUtil.loadIndividualRowDetails(synapseClient, nodeModelCreator, isLoggedIn, records, rowIndex, new RowLoadedHandler() {					
					@Override
					public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay) {
						view.setEntityGroupRecordDisplay(rowIndex, entityGroupRecordDisplay, isLoggedIn);
					}
				});
			}			
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/*
	 * Private Methods
	 */

}
