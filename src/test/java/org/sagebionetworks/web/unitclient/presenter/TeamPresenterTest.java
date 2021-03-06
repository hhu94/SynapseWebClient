package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.presenter.TeamPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TeamPresenterTest {

	TeamPresenter presenter;
	TeamView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalAppState;
	SynapseClientAsync mockSynClient;
	SynapseAlert mockSynAlert;
	TeamLeaveModalWidget mockLeaveModal;
	TeamDeleteModalWidget mockDeleteModal;
	TeamEditModalWidget mockEditModal;
	InviteWidget mockInviteModal;
	JoinTeamWidget mockJoinWidget;
	MemberListWidget mockMemberListWidget;
	OpenMembershipRequestsWidget mockOpenMembershipRequestsWidget;
	OpenUserInvitationsWidget mockOpenUserInvitationsWidget;
	Team mockTeam;
	TeamBundle mockTeamBundle;
	TeamMembershipStatus mockTeamMembershipStatus;
	
	String teamId = "123";
	String teamName = "testTeam";
	boolean canPublicJoin = true;
	String teamIcon = "teamIcon";
	Long totalMembershipCount = 3L;
	Exception caught = new Exception("this is an exception");
	
	String newName = "newName";
	String newDesc = "newDesc";
	boolean newPublicJoin = false;
	String newIcon = "newIcon";
	@Mock
	GoogleMap mockGoogleMap;
	@Mock
	CookieProvider mockCookies;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackPcaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(TeamView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalAppState = mock(GlobalApplicationState.class);
		mockSynClient = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockLeaveModal = mock(TeamLeaveModalWidget.class);
		mockDeleteModal = mock(TeamDeleteModalWidget.class);
		mockEditModal = mock(TeamEditModalWidget.class);
		mockInviteModal = mock(InviteWidget.class);
		mockJoinWidget = mock(JoinTeamWidget.class);
		mockMemberListWidget = mock(MemberListWidget.class);
		mockOpenMembershipRequestsWidget = mock(OpenMembershipRequestsWidget.class);
		mockOpenUserInvitationsWidget = mock(OpenUserInvitationsWidget.class);
		presenter = new TeamPresenter(mockView, mockAuthenticationController, mockGlobalAppState, 
				mockSynClient, mockSynAlert, mockLeaveModal, mockDeleteModal, mockEditModal, 
				mockInviteModal, mockJoinWidget, mockMemberListWidget, 
				mockOpenMembershipRequestsWidget, mockOpenUserInvitationsWidget, mockGoogleMap, mockCookies,
				mockIsACTMemberAsyncHandler);
		mockTeam = mock(Team.class);
		when(mockTeam.getName()).thenReturn(teamName);
		mockTeamBundle = mock(TeamBundle.class);
		mockTeamMembershipStatus = mock(TeamMembershipStatus.class);
		AsyncMockStubber.callSuccessWith(mockTeamBundle).when(mockSynClient)
		.getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		//team bundle
		when(mockTeamBundle.getTeam()).thenReturn(mockTeam);
		when(mockTeamBundle.getTeamMembershipStatus()).thenReturn(mockTeamMembershipStatus);
		when(mockTeamBundle.getTotalMemberCount()).thenReturn(totalMembershipCount);
		
		//team
		when(mockTeam.getCanPublicJoin()).thenReturn(canPublicJoin);
		when(mockTeam.getId()).thenReturn(teamId);
		when(mockTeam.getIcon()).thenReturn(teamIcon);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("true");
	}
	
	private void setIsACT(boolean isACT) {
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPcaptor.capture());
		callbackPcaptor.getValue().invoke(isACT);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(any(Widget.class));
		verify(mockView).setLeaveTeamWidget(any(Widget.class));
		verify(mockView).setDeleteTeamWidget(any(Widget.class));
		verify(mockView).setEditTeamWidget(any(Widget.class));
		verify(mockView).setInviteMemberWidget(any(Widget.class));
		verify(mockView).setJoinTeamWidget(any(Widget.class));
		verify(mockView).setOpenMembershipRequestWidget(any(Widget.class));
		verify(mockView).setOpenUserInvitationsWidget(any(Widget.class));
		verify(mockView).setMemberListWidget(any(Widget.class));
		verify(mockLeaveModal).setRefreshCallback(any(Callback.class));
		verify(mockEditModal).setRefreshCallback(any(Callback.class));
		verify(mockDeleteModal).setRefreshCallback(any(Callback.class));
		verify(mockInviteModal).setRefreshCallback(any(Callback.class));
		verify(mockView).setMap(any(Widget.class));
	}
	
	@Test
	public void testRefreshFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynClient).getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		presenter.refresh(teamId);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testRefreshAdmin() {
		boolean isAdmin = true;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(true);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(canPublicJoin);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockView).showMemberMenuItems();
		verify(mockOpenMembershipRequestsWidget).setVisible(true);
		verify(mockView).showAdminMenuItems();
		verify(mockView).setTeamEmailAddress(anyString());
		//never
		verify(mockJoinWidget, never()).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
	}
	
	@Test
	public void testIsACT() {
		presenter.refresh(teamId);
		setIsACT(true);
		verify(mockView).setManageAccessVisible(true);
	}
	@Test
	public void testIsNotACT() {
		presenter.refresh(teamId);
		setIsACT(false);
		verify(mockView).setManageAccessVisible(false);
	}
	
	@Test
	public void testRefreshNotMember() {
		boolean isAdmin = false;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(false);
		//SWC-2655: also test null canPublicJoin
		when(mockTeam.getCanPublicJoin()).thenReturn(null);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(false);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockJoinWidget).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
		verify(mockOpenMembershipRequestsWidget).setVisible(false);
		//never
		verify(mockView, never()).showMemberMenuItems();
		verify(mockOpenMembershipRequestsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockOpenUserInvitationsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockView, never()).showAdminMenuItems();
	}
	
	@Test
	public void testRefreshMember() {	
		boolean isAdmin = false;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(true);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(canPublicJoin);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockView).showMemberMenuItems();
		
		//never
		verify(mockJoinWidget, never()).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
		verify(mockOpenMembershipRequestsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockOpenUserInvitationsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockView, never()).showAdminMenuItems();
		
		verify(mockGoogleMap, never()).configure(teamId);
		verify(mockView).setShowMapVisible(false);
		
		//simulate clicking Show Map
		presenter.onShowMap();
		verify(mockGoogleMap).setHeight(anyString());
		verify(mockGoogleMap).configure(teamId);
		verify(mockView).showMapModal();
		verify(mockOpenMembershipRequestsWidget).setVisible(false);
	}

	@Test
	public void testGetTeamEmail() {
		boolean canSendEmail = true;
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		assertEquals("basic@synapse.org", presenter.getTeamEmail("basic", canSendEmail));
		assertEquals("StandardCaseHere@synapse.org", presenter.getTeamEmail("Standard Case Here", canSendEmail));
		assertEquals("unlikelycase@synapse.org", presenter.getTeamEmail(" \n\r unlikely\t case ", canSendEmail));
		assertEquals("Another_UnlikelyCase@synapse.org", presenter.getTeamEmail(" %^$##* Another_Unlikely\t &*#$)(!!@~Case ", canSendEmail));
		
		canSendEmail = false;
		assertEquals("", presenter.getTeamEmail("basic", canSendEmail));
		
		canSendEmail = true;
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		assertEquals("", presenter.getTeamEmail("basic", canSendEmail));
	}
	
	@Test
	public void testRefreshOpenMembershipRequests() {
		presenter.refreshOpenMembershipRequests();
		verify(mockOpenMembershipRequestsWidget).clear();
		verify(mockOpenMembershipRequestsWidget).configure(anyString(), callbackCaptor.capture());
		Callback callback = callbackCaptor.getValue();
		callback.invoke();
		// reconfigured widget, and refreshed
		verify(mockOpenMembershipRequestsWidget, times(2)).configure(anyString(), eq(callback));
		verify(mockSynClient).getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		// never reconfigures open invites (no need to refresh)
		verify(mockOpenUserInvitationsWidget, never()).configure(anyString(), eq(callback));
	}
	
	@Test
	public void testRefreshOpenUserInvitations() {
		presenter.refreshOpenUserInvitations();
		verify(mockOpenUserInvitationsWidget).clear();
		verify(mockOpenUserInvitationsWidget).configure(anyString(), callbackCaptor.capture());
		Callback callback = callbackCaptor.getValue();
		callback.invoke();
		// reconfigured widget, and refreshed
		verify(mockOpenUserInvitationsWidget, times(2)).configure(anyString(), eq(callback));
		verify(mockSynClient).getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		// never reconfigures open membership requests (no need to refresh)
		verify(mockOpenMembershipRequestsWidget, never()).configure(anyString(), eq(callback));
	}

}
