package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1.EMPTY_SCOPE_MESSAGE;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;



public class CreateTableViewWizardStep1Test {

	@Mock
	CreateTableViewWizardStep1View mockView;
	@Mock
	ModalPresenter mockWizardPresenter;
	@Mock
	EntityContainerListWidget mockEntityContainerListWidget;
	@Mock
	CreateTableViewWizardStep2 mockStep2;
	
	@Mock
	SynapseJavascriptClient mockJsClient;
	String parentId;
	CreateTableViewWizardStep1 widget;
	List<String> scopeIds;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		scopeIds = Collections.singletonList("3");
		when(mockEntityContainerListWidget.getEntityIds()).thenReturn(scopeIds);
		widget = new CreateTableViewWizardStep1(mockView, mockJsClient, mockEntityContainerListWidget, mockStep2);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
	}
	
	@Test
	public void testNullName(){
		widget.configure(parentId, TableType.fileview);
		when(mockView.getName()).thenReturn(null);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(CreateTableViewWizardStep1.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockJsClient, never()).createEntity(any(Entity.class));
	}
	
	@Test
	public void testCreateFileView(){
		widget.configure(parentId, TableType.fileview);
		verify(mockView).setName("");
		verify(mockView).setScopeWidgetVisible(true);
		
		verify(mockView).setFileViewTypeSelectionVisible(true);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		EntityView capturedFileView = (EntityView)captor.getValue();
		assertEquals(scopeIds, capturedFileView.getScopeIds());
		assertEquals(ViewType.file, capturedFileView.getType());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.fileview);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}
	
	@Test
	public void testCreateFileViewEmptyScope(){
		widget.configure(parentId, TableType.fileview);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		when(mockJsClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		when(mockEntityContainerListWidget.getEntityIds()).thenReturn(Collections.EMPTY_LIST);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(EMPTY_SCOPE_MESSAGE);
	}
	
	@Test
	public void testCreateProjectView(){
		widget.configure(parentId, TableType.projectview);
		verify(mockView).setName("");
		verify(mockView).setScopeWidgetVisible(true);
		verify(mockView).setFileViewTypeSelectionVisible(false);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		EntityView capturedFileView = (EntityView)captor.getValue();
		assertEquals(scopeIds, capturedFileView.getScopeIds());
		assertEquals(ViewType.project, capturedFileView.getType());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.projectview);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}
	
	@Test
	public void testCreateTable(){
		widget.configure(parentId, TableType.table);
		verify(mockView).setScopeWidgetVisible(false);
		verify(mockView).setFileViewTypeSelectionVisible(false);
		String tableName = "a name";
		TableEntity table = new TableEntity();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		TableEntity capturedTable = (TableEntity)captor.getValue();
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.table);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);;
	}
	
	@Test
	public void testCreateFailed(){
		widget.configure(parentId, TableType.fileview);
		String tableName = "a name";
		String error = "name already exists";
		when(mockJsClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new Throwable(error)));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(error);

		//TODO: should not go to the next step
		verify(mockWizardPresenter, never()).onFinished();
	}
	
}
