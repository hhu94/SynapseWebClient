package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EntityPresenterTest {
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	EntityPresenter entityPresenter;
	EntityView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	CookieProvider mockCookies;
	PlaceChanger mockPlaceChanger;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	StuAlert mockSynAlert;
	OpenTeamInvitationsWidget mockOpenInviteWidget;
	Header mockHeaderWidget;
	EntityPageTop mockEntityPageTop;
	String EntityId = "1";
	Synapse place = new Synapse("Synapse:"+ EntityId);
	Entity EntityModel1;
	EntityBundle eb;
	String entityId = "syn43344";
	Synapse.EntityArea area = Synapse.EntityArea.FILES;
	String areaToken = null;
	long id;
	
	String rootWikiId = "12333";
	FileHandleResults rootWikiAttachments;
	GWTWrapper gwtWrapper;
	
	@Before
	public void setup() throws Exception{
		MockitoAnnotations.initMocks(this);
		mockView = mock(EntityView.class);
		gwtWrapper = mock(GWTWrapper.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockCookies = mock(CookieProvider.class);
		mockSynAlert = mock(StuAlert.class);
		mockOpenInviteWidget = mock(OpenTeamInvitationsWidget.class);
		mockHeaderWidget = mock(Header.class);
		mockEntityPageTop = mock(EntityPageTop.class);
		entityPresenter = new EntityPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseJavascriptClient,
				mockCookies, mockSynAlert, mockEntityPageTop, mockHeaderWidget, mockOpenInviteWidget, gwtWrapper);
		Entity testEntity = new Project();
		eb = new EntityBundle();
		eb.setEntity(testEntity);
		EntityPath path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		id=0L;
		when(mockGlobalApplicationState.isWikiBasedEntity(anyString())).thenReturn(false);
	}	
	
	@Test
	public void testConstruction() {
		verify(mockEntityPageTop).setEntityUpdatedHandler(any(EntityUpdatedHandler.class));
		verify(mockHeaderWidget, never()).configure(false); // waits to configure for entity header
	}
	
	@Test
	public void testSetPlaceAndRefreshWithVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(1L);
		when(place.getEntityId()).thenReturn(entityId);
		
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockGlobalApplicationState).isWikiBasedEntity(entityId);
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop, atLeastOnce()).clearState();
		verify(mockEntityPageTop).configure(eq(eb.getEntity()), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
		verify(mockView).setOpenTeamInvitesWidget(mockOpenInviteWidget);
		verify(mockHeaderWidget).refresh();
		verify(mockHeaderWidget).configure(eq(false), any(EntityHeader.class));
	}
	
	@Test
	public void testSetPlaceAndRefreshWithoutVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockGlobalApplicationState).isWikiBasedEntity(entityId);
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop, atLeastOnce()).clearState();
		verify(mockEntityPageTop).configure(eq(eb.getEntity()), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
	}
	
	@Test
	public void testStart() {
		entityPresenter.setPlace(place);		
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		entityPresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);		
	}
	
	@Test
	public void testSetPlaceAndRefreshWikiBasedEntity() {
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		//redirects to the wiki place
		verify(mockPlaceChanger).goTo(isA(Wiki.class));
		verify(mockView, never()).setEntityPageTopVisible(true);
	}
	
	@Test
	public void testSetPlaceAndRefreshWikiBasedEntityInTestWebsite() {
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		Exception caught = new Exception("test");
		//will show full project page for wiki based entities when in alpha mode
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityPresenter.setPlace(place);
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		//verify synapse client call
		verify(mockSynAlert).handleException(caught);
	}
	
	private AccessRequirement createNewAR(ACCESS_TYPE type) {
		AccessRequirement ar = new TermsOfUseAccessRequirement();
		ar.setAccessType(type);
		id++;
		ar.setId(id);
		return ar;
	}
	
	@Test
	public void testSetPlaceAndRefreshFailure() {
		Exception caught = new Exception("test");
		//will show full project page for wiki based entities when in alpha mode
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
		//header should be reconfigured to set back to Synapse
		verify(mockHeaderWidget).configure(false);
	}
	
	@Test
	public void testInvalidEntityIdInPlace() {
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		String entityId = "syn";
		Synapse place = new Synapse(entityId, null, area, areaToken);
		entityPresenter.setPlace(place);
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(gwtWrapper).scheduleDeferred(captor.capture());
		//invoke callback
		captor.getValue().invoke();
		
		verify(mockSynAlert).show404();
		//header should be reconfigured to set back to Synapse
		verify(mockHeaderWidget).configure(false);
	}
	
	@Test
	public void testClear() {
		entityPresenter.clear();
		verify(mockView, times(2)).clear();
		verify(mockSynAlert, times(2)).clear();
		verify(mockOpenInviteWidget, times(2)).clear();
	}
	
	@Test
	public void testShow403() {
		entityPresenter.setEntityId("123");
		entityPresenter.show403();
		verify(mockSynAlert).show403(anyString());
		verify(mockView).setEntityPageTopVisible(false);
		verify(mockView).setOpenTeamInvitesVisible(true);
	}
	
	@Test
	public void testEntityUpdatedHandler() {
		ArgumentCaptor<EntityUpdatedHandler> captor = ArgumentCaptor.forClass(EntityUpdatedHandler.class);
		verify(mockEntityPageTop).setEntityUpdatedHandler(captor.capture());
		//invoke and verify
		captor.getValue().onPersistSuccess(null);
		
		verify(mockGlobalApplicationState).refreshPage();
	}
	
	@Test
	public void testIsValidEntityId() {
		assertFalse(entityPresenter.isValidEntityId(""));
		assertFalse(entityPresenter.isValidEntityId(null));
		assertFalse(entityPresenter.isValidEntityId("syn"));
		assertFalse(entityPresenter.isValidEntityId("sy"));
		assertFalse(entityPresenter.isValidEntityId("synFOOBAR"));
		assertTrue(entityPresenter.isValidEntityId("SyN198327"));
	}
}
