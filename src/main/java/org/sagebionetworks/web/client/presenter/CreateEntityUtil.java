package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateEntityUtil {

	public static void createProject(final String name,
			SynapseClientAsync synapseClient,
			final AsyncCallback<String> callback) {
		Project proj = new Project();
		proj.setEntityType(Project.class.getName());
		proj.setName(name);		
		synapseClient.createOrUpdateEntity(proj, null, true, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newProjectId) {
				callback.onSuccess(newProjectId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});	
	}

}
