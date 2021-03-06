package org.sagebionetworks.web.unitclient.widget.docker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerRepoListWidgetTest {
	@Mock
	private DockerRepoListWidgetView mockView;
	@Mock
	private BasicPaginationWidget mockPaginationWidget;
	@Mock
	private Project mockProject;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private LoadMoreWidgetContainer mockMembersContainer;
	@Mock
	EntityChildrenResponse mockResults;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	
	List<EntityHeader> searchResults;
	
	DockerRepoListWidget dockerRepoListWidget;
	String projectId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoListWidget = new DockerRepoListWidget(
				mockView,
				mockMembersContainer,
				mockSynAlert, 
				mockSynapseJavascriptClient);
		projectId = "syn123";
		when(mockProject.getId()).thenReturn(projectId);
		AsyncMockStubber.callSuccessWith(mockResults).when(mockSynapseJavascriptClient)
			.getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		searchResults = new ArrayList<EntityHeader>();
		when(mockResults.getPage()).thenReturn(searchResults);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setMembersContainer(mockMembersContainer);
		verify(mockView).setSynAlert(any(Widget.class));
	}

	@Test
	public void testAsWidget() {
		dockerRepoListWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testCreateDockerRepoEntityQuery() {
		EntityChildrenRequest query = dockerRepoListWidget.createDockerRepoEntityQuery(projectId);
		assertEquals(projectId, query.getParentId());
		assertEquals(Collections.singletonList(EntityType.dockerrepo), query.getIncludeTypes());
		assertEquals(SortBy.CREATED_ON, query.getSortBy());
		assertEquals(Direction.DESC, query.getSortDirection());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccess() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository bundle1 = new DockerRepository();
		DockerRepository bundle2 = new DockerRepository();
		AsyncMockStubber.callSuccessWith(bundle1, bundle2)
			.when(mockSynapseJavascriptClient).getEntity(anyString(), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		//simulate that view is attached
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView, atLeastOnce()).addRepo(bundle1);
		verify(mockView, atLeastOnce()).addRepo(bundle2);
		verify(mockSynapseJavascriptClient).getEntity(eq(id1), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntity(eq(id2), any(OBJECT_TYPE.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccessOverOnePage() {
		dockerRepoListWidget.configure(projectId);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationQueryFailure() {
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error)
			.when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView, never()).addRepo(any(DockerRepository.class));
		verify(mockSynAlert).handleException(error);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationFailToGetSecondDockerRepository() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository bundle = new DockerRepository();
		AsyncMockStubber.callSuccessWith(bundle)
			.when(mockSynapseJavascriptClient).getEntity(eq(id1), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error)
			.when(mockSynapseJavascriptClient).getEntity(eq(id2), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView).addRepo(bundle);
		verify(mockSynapseJavascriptClient).getEntity(eq(id1), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntity(eq(id2), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(error);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationFailToGetFirstDockerRepository() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository bundle = new DockerRepository();
		AsyncMockStubber.callSuccessWith(bundle)
			.when(mockSynapseJavascriptClient).getEntity(eq(id2), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error)
			.when(mockSynapseJavascriptClient).getEntity(eq(id1), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).addRepo(bundle);
		verify(mockSynapseJavascriptClient).getEntity(eq(id1), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntity(eq(id2), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(error);
	}

	@Test
	public void testLoadMore() {
		when(mockResults.getNextPageToken()).thenReturn("not null");
		dockerRepoListWidget.configure(projectId);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockMembersContainer, times(2)).setIsMore(true);
		when(mockResults.getNextPageToken()).thenReturn(null);
		dockerRepoListWidget.loadMore();
		verify(mockSynapseJavascriptClient, times(2)).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockMembersContainer).setIsMore(false);
	}
}
