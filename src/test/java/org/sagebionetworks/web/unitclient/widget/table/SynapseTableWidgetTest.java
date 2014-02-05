package org.sagebionetworks.web.unitclient.widget.table;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.table.SynapseTableWidget;
import org.sagebionetworks.web.client.widget.table.SynapseTableWidgetView;


public class SynapseTableWidgetTest {
	
	SynapseTableWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	PlaceChanger mockPlaceChanger;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	
	SynapseTableWidget tableWidget;
	TableEntity table;
	
	@Before
	public void setup(){		
		mockView = mock(SynapseTableWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockAuthenticationController = mock(AuthenticationController.class);
				
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		
		tableWidget = new SynapseTableWidget(mockView, mockSynapseClient, mockAuthenticationController, adapterFactory, mockGlobalApplicationState);
		
		table = new TableEntity();
	}
	
	@Test
	public void testConfigure() {
		tableWidget.configure(table);		
	}

}