package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererImpl implements EntityIdCellRenderer{

	EntityIdCellRendererView view;
	LazyLoadHelper lazyLoadHelper;
	EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	SynapseJSNIUtils jsniUtils;
	String entityId, entityName;
	ClickHandler customClickHandler;
	boolean hideIfLoadError;
	@Inject
	public EntityIdCellRendererImpl(EntityIdCellRendererView view, 
			LazyLoadHelper lazyLoadHelper,
			EntityHeaderAsyncHandler entityHeaderAsyncHandler,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.lazyLoadHelper = lazyLoadHelper;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		this.jsniUtils = jsniUtils;
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				loadData();
			}
		};
		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void loadData() {
		if (entityName == null && entityId != null) {
			view.showLoadingIcon();
			String requestEntityId = entityId.toLowerCase().startsWith("syn") ? entityId : "syn"+entityId;
			if (customClickHandler == null) {
				view.setLinkHref(Synapse.getHrefForDotVersion(requestEntityId));	
			} else {
				view.setClickHandler(customClickHandler);
			}
			
			entityHeaderAsyncHandler.getEntityHeader(requestEntityId, new AsyncCallback<EntityHeader>() {
				@Override
				public void onSuccess(EntityHeader entity) {
					entityName = entity.getName();
					view.setIcon(EntityTypeUtils.getIconTypeForEntityClassName(entity.getType()));
					view.setLinkText(entityName);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (hideIfLoadError) {
						jsniUtils.consoleError(caught.getMessage());
						view.setVisible(false);
					} else {
						view.showErrorIcon(caught.getMessage());
						view.setLinkText(entityId);
					}
				}
			});
		}
	}
	
	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void setValue(String value, boolean hideIfLoadError) {
		setValue(value, null, hideIfLoadError);
	}

	@Override
	public void setValue(String value) {
		setValue(value, null, false);
	}
	
	public void setValue(String value, ClickHandler customClickHandler, boolean hideIfLoadError) {
		view.hideAllIcons();
		this.entityId = value;
		this.hideIfLoadError = hideIfLoadError;
		entityName = null;
		this.customClickHandler = customClickHandler;
		lazyLoadHelper.setIsConfigured();
	}

	@Override
	public String getValue() {
		return entityId;
	}


	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
}
