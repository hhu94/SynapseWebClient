package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererDateTest {
		
	@Mock
	DateTimeUtils mockDateTimeUtils;
	
	APITableColumnRendererDate renderer;
	GWTWrapper mockGwt;
	Map<String, List<String>> columnData;
	APITableColumnConfig config;
	AsyncCallback<APITableInitializedColumnRenderer> mockCallback;
	String inputColumnName = "name";
	String formattedDate = "May the 4th";
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		mockGwt = mock(GWTWrapper.class);
		DateTimeFormat mockDateTimeFormat = mock(DateTimeFormat.class);
		when(mockGwt.getDateTimeFormat(PredefinedFormat.ISO_8601)).thenReturn(mockDateTimeFormat);
		when(mockDateTimeFormat.format(any(Date.class))).thenReturn(formattedDate);
		when(mockDateTimeUtils.convertDateToSmallString(any(Date.class))).thenReturn(formattedDate);
		renderer = new APITableColumnRendererDate(mockGwt, mockDateTimeUtils);
		columnData = new HashMap<String, List<String>>();
		config = new APITableColumnConfig();
		HashSet<String> inputColumnNames = new HashSet<String>();
		inputColumnNames.add(inputColumnName);
		config.setInputColumnNames(inputColumnNames);
		mockCallback = mock(AsyncCallback.class);
		APITableTestUtils.setInputValue("2014-05-04T13:57:17.632Z", inputColumnName, columnData);
	}
	
	@Test
	public void testInitHappy() {
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//output column name same as input
		assertTrue(initializedRenderer.getColumnData().containsKey(inputColumnName));
		assertEquals(formattedDate, initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	
	@Test
	public void testInitLongDate() {
		APITableTestUtils.setInputValue("1399989680999", inputColumnName, columnData);
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		assertEquals(formattedDate, initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	
	@Test
	public void testInitNull() {
		APITableTestUtils.setInputValue(null, inputColumnName, columnData);
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//null value should be rendered as an empty string
		assertEquals("", initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	//Callback.onFailure is never called.  An empty column is shown if the data are unavailable for the specified column.
}
