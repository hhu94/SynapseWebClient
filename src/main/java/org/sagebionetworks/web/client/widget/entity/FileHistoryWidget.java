package org.sagebionetworks.web.client.widget.entity;

import java.util.Objects;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget shows the properties and annotations as a non-editable table grid.
 *
 * @author jayhodgson
 */
public class FileHistoryWidget implements FileHistoryWidgetView.Presenter, IsWidget, PageChangeListener {
	
	private FileHistoryWidgetView view;
	private EntityBundle bundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	public static final Integer VERSION_LIMIT = 100;
	public PreflightController preflightController;
	
	private BasicPaginationWidget paginationWidget;
	private boolean canEdit;
	private Long versionNumber;
	@Inject
	public FileHistoryWidget(FileHistoryWidgetView view,
			 SynapseClientAsync synapseClient, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController,
			 BasicPaginationWidget paginationWidget,
			 PreflightController preflightController) {
		super();
		this.synapseClient = synapseClient;
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.paginationWidget = paginationWidget;
		this.preflightController = preflightController;
		view.setPaginationWidget(paginationWidget.asWidget());
		this.view.setPresenter(this);
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber, boolean isCurrentVersion) {
		this.bundle = bundle;
		this.versionNumber = versionNumber;
		this.canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		view.setEntityBundle(bundle.getEntity(), !isCurrentVersion);
		view.setEditVersionInfoButtonVisible(isCurrentVersion && canEdit);
		
		//initialize versions
		onPageChange(WebConstants.ZERO_OFFSET);
	}

	@Override
	public void updateVersionInfo(String newLabel, String newComment) {
		editCurrentVersionInfo(bundle.getEntity(), newLabel, newComment);
	}

	private void editCurrentVersionInfo(Entity entity, String version, String comment) {
		if (entity instanceof VersionableEntity) {
			final VersionableEntity vb = (VersionableEntity)entity;
			if (Objects.equals(version, vb.getVersionLabel()) &&
				Objects.equals(comment, vb.getVersionComment())) {
				// no-op
				view.hideEditVersionInfo();
				return;
			}
			String versionLabel = null;
			if (version!=null)
				versionLabel = version.toString();
			vb.setVersionLabel(versionLabel);
			vb.setVersionComment(comment);
			
			synapseClient.updateEntity(vb,
					new AsyncCallback<Entity>() {
						@Override
						public void onFailure(Throwable caught) {
							if (!DisplayUtils.handleServiceException(
									caught, globalApplicationState,
									authenticationController.isLoggedIn(), view)) {
								view.showEditVersionInfoError(DisplayConstants.ERROR_UPDATE_FAILED
										+ ": " + caught.getMessage());
							}
						}
						
						@Override
						public void onSuccess(Entity result) {
							view.hideEditVersionInfo();
							view.showInfo(DisplayConstants.VERSION_INFO_UPDATED, "Updated " + vb.getName());
							fireEntityUpdatedEvent();
						}
					});
		}
	}
	
	@Override
	public void deleteVersion(final Long versionNumber) {
		synapseClient.deleteEntityVersionById(bundle.getEntity().getId(), versionNumber, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught,
						globalApplicationState,
						authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + "\n" + caught.getMessage());
				}
			}
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Version deleted", "Version "+ versionNumber + " of " + bundle.getEntity().getId() + " " + DisplayConstants.LABEL_DELETED);
				fireEntityUpdatedEvent();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return view.asWidget();
	}
	
	
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}
	
	@Override
	public void onPageChange(final Long newOffset) {
		view.clearVersions();
		synapseClient.getEntityVersions(bundle.getEntity().getId(), newOffset.intValue(), VERSION_LIMIT,
			new AsyncCallback<PaginatedResults<VersionInfo>>() {
				@Override
				public void onSuccess(PaginatedResults<VersionInfo> result) {
					PaginatedResults<VersionInfo> paginatedResults;
					paginatedResults = result;
					paginationWidget.configure(VERSION_LIMIT.longValue(), newOffset, paginatedResults.getTotalNumberOfResults(), FileHistoryWidget.this);
					if (versionNumber == null && newOffset == 0 && paginatedResults.getResults().size() > 0) {
						versionNumber = paginatedResults.getResults().get(0).getVersionNumber();
					}
					for (VersionInfo versionInfo : paginatedResults.getResults()) {
						view.addVersion(versionInfo, canEdit, versionInfo.getVersionNumber().equals(versionNumber));
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
	}

	
	/**
	 * For testing purposes only
	 * @return
	 */
	public Long getVersionNumber() {
		return versionNumber;
	}
	
	@Override
	public void onEditVersionInfoClicked() {
		preflightController.checkUploadToEntity(bundle, new Callback() {
			@Override
			public void invoke() {
				final VersionableEntity vb = (VersionableEntity)bundle.getEntity();
				view.showEditVersionInfo(vb.getVersionLabel(), vb.getVersionComment());
			}
		});
	}
}
