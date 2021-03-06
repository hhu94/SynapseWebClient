package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.repo.model.util.ContentTypeUtils.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget.PreviewFileType;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Unit test for the preview widget.
 * @author jayhodgson
 *
 */
public class PreviewWidgetTest {
	PreviewWidget previewWidget;
	@Mock
	PreviewWidgetView mockView;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	EntityBundle testBundle;
	FileEntity testEntity;
	List<FileHandle> testFileHandleList;
	@Mock
	Response mockResponse;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	VideoWidget mockVideoWidget;
	@Mock
	EntityBundle linkBundle;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	PDFPreviewWidget mockPDFPreviewWidget;
	@Mock
	HtmlPreviewWidget mockHtmlPreviewWidget;
	@Mock
	NbConvertPreviewWidget mockNbConvertPreviewWidget;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	FileHandle mainFileHandle;
	String zipTestString = "base.jar\ntarget/\ntarget/directory/\ntarget/directory/test.txt\n";
	Map<String, String> descriptor;
	public static final String TEST_ENTITY_ID = "syn20923";
	public static final String TEST_ENTITY_MAIN_FILE_CREATED_BY = "8992983";
	@Before
	public void before() throws Exception{
		MockitoAnnotations.initMocks(this);
		previewWidget = new PreviewWidget(mockView, mockRequestBuilder, mockSynapseJSNIUtils, mockSynapseAlert, mockSynapseClient, mockAuthController, mockSynapseJavascriptClient, mockPortalGinInjector);
		testEntity = new FileEntity();
		testEntity.setId(TEST_ENTITY_ID);
		testFileHandleList = new ArrayList<FileHandle>();
		mainFileHandle = new S3FileHandle();
		String mainFileId = "MAIN_FILE";
		mainFileHandle.setId(mainFileId);
		mainFileHandle.setCreatedBy(TEST_ENTITY_MAIN_FILE_CREATED_BY);
		testFileHandleList.add(mainFileHandle);
		testEntity.setDataFileHandleId(mainFileId);
		testBundle = new EntityBundle();
		testBundle.setEntity(testEntity);
		testBundle.setFileHandles(testFileHandleList);
		when(mockPortalGinInjector.getVideoWidget()).thenReturn(mockVideoWidget);
		when(mockPortalGinInjector.getPDFPreviewWidget()).thenReturn(mockPDFPreviewWidget);
		when(mockPortalGinInjector.getHtmlPreviewWidget()).thenReturn(mockHtmlPreviewWidget);
		when(mockPortalGinInjector.getNbConvertPreviewWidget()).thenReturn(mockNbConvertPreviewWidget);
		when(mockSynapseJSNIUtils.getBaseFileHandleUrl()).thenReturn("http://fakebaseurl/");
		mockResponse = mock(Response.class);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn(zipTestString);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		when(mockSynapseAlert.isUserLoggedIn()).thenReturn(true);
		
		AsyncMockStubber.callSuccessWith(testBundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testBundle).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		
		// create empty wiki descriptor
		descriptor = new HashMap<String, String>();
	}
	@Test
	public void testWrongEntityType(){
		Project project = new Project();
		project.setName("Test only");
		project.setId("99");
		testBundle.setEntity(project);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		
		verify(mockView).addSynapseAlertWidget(any(Widget.class));
		verify(mockSynapseAlert).showError(anyString());
	}
	
	@Test
	public void testNoPreviewFileHandleAvailable(){
		S3FileHandle fh = new S3FileHandle();
		fh.setId("previewFileId");
		fh.setContentType("image/png");
		fh.setFileName("preview.png");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView, times(0)).setImagePreview(anyString());
	}
	
	@Test
	public void testPreviewImageContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setFileName("preview.png");
		fh.setContentType("image/png");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setImagePreview(anyString());
	}
	
	
	@Test
	public void testPreviewHtmlContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("text/plain");
		mainFileHandle.setContentType("text/html");
		assertEquals(PreviewFileType.HTML, previewWidget.getOriginalFileType(mainFileHandle));
	}
	
	@Test
	public void testHtmlContentType(){
		mainFileHandle.setContentType("text/html");
		assertEquals(PreviewFileType.NONE, previewWidget.getPreviewFileType(null, mainFileHandle));
		assertEquals(PreviewFileType.HTML, previewWidget.getOriginalFileType(mainFileHandle));
	}
	
	@Test
	public void testPreviewSvgImageContentType(){
		// images that do not have a preview file handle will use the original
		mainFileHandle.setContentType("image/svg+xml");
		mainFileHandle.setFileName("original.svg");
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setImagePreview(anyString());
	}
	
	@Test
	public void testPreviewPngImageContentType(){
		// images that do not have a preview file handle will use the original
		mainFileHandle.setContentType("image/png");
		mainFileHandle.setFileName("original.png");
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setImagePreview(anyString());
	}
	
	@Test
	public void testMainFilePdfContentType(){
		// images that do not have a preview file handle will use the original
		mainFileHandle.setContentType("application/pdf");
		mainFileHandle.setFileName("original.pdf");
		previewWidget.configure(testBundle);
		verify(mockView).setPreviewWidget(mockPDFPreviewWidget);
	}
	
	@Test
	public void testPreviewFilePdfContentType(){
		mainFileHandle.setContentType("doc");
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("application/pdf");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setPreviewWidget(mockPDFPreviewWidget);
	}
	
	@Test
	public void testPreviewCodeContentType(){
		mainFileHandle.setFileName("codeFile.R");
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType(ContentTypeUtils.PLAIN_TEXT);
		fh.setFileName("preview.txt");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setCodePreview(anyString(), eq("r"));
	}

	@Test
	public void testPreviewOtherTextContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		String invalidContentType = "text/other";
		fh.setContentType(invalidContentType);
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setTextPreview(anyString());
	}
	
	@Test
	public void testZipContentType(){
		mainFileHandle.setContentType(PreviewWidget.APPLICATION_ZIP);
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("text/csv");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setTextPreview(anyString());
	}
	
	@Test
	public void testVideoFilename(){
		mainFileHandle.setContentType(APPLICATION_OCTET_STREAM);
		mainFileHandle.setFileName("testvideo.mp4");
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setPreviewWidget(mockVideoWidget);
	}
	
	@Test
	public void testLongContent(){
		mainFileHandle.setContentType(PreviewWidget.APPLICATION_ZIP);
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("text/plain");
		char[] charArray = new char[PreviewWidget.MAX_LENGTH + 100];
	    Arrays.fill(charArray, 'a');
	    when(mockResponse.getText()).thenReturn(new String(charArray));
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setTextPreview(captor.capture());
		String textSentToView = captor.getValue();
		assertEquals(PreviewWidget.MAX_LENGTH + "...".length(), textSentToView.length());
	}
	
	@Test
	public void testPreviewVideoContentType(){
		mainFileHandle.setFileName("preview.mp4");
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("video/mp4");
		fh.setFileName("preview.mp4");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setPreviewWidget(any(Widget.class));
	}
	
	@Test
	public void testPreviewFileWithNoFileHandle() {
		testBundle.setFileHandles(new ArrayList<FileHandle>());
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView, times(0)).setTextPreview(anyString());
		verify(mockView, times(0)).setCodePreview(anyString(), anyString());
		verify(mockView, times(0)).setTablePreview(anyString(), anyString());
		verify(mockView, times(0)).setImagePreview(anyString());
		verify(mockView, times(0)).setPreviewWidget(any(Widget.class));
	}
	
	@Test
	public void testFollowLink(){
		Link link = new Link();
		Reference ref = new Reference();
		String targetEntityId = "syn9876";
		ref.setTargetId(targetEntityId);
		link.setLinksTo(ref);
		
		when(linkBundle.getEntity()).thenReturn(link);
		previewWidget.configure(linkBundle);
		previewWidget.asWidget();
		
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(targetEntityId), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testFollowLinkWithVersion(){
		Link link = new Link();
		Reference ref = new Reference();
		String targetEntityId = "syn9876";
		Long targetVersion = 882L;
		ref.setTargetVersionNumber(targetVersion);
		ref.setTargetId(targetEntityId);
		link.setLinksTo(ref);
		
		when(linkBundle.getEntity()).thenReturn(link);
		previewWidget.configure(linkBundle);
		previewWidget.asWidget();
		
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(targetEntityId), eq(targetVersion), anyInt(), any(AsyncCallback.class));
	}

	
	@Test
	public void testWikiConfigure() {		
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, "syn111");
		previewWidget.configure(null, descriptor, null, null);
		
		//verify that it tries to get the entity bundle (without version)
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testWikiConfigureWithVersion() {		
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, "syn111");
		descriptor.put(WidgetConstants.WIDGET_ENTITY_VERSION_KEY, "1");
		previewWidget.configure(null, descriptor, null, null);
		
		//verify that it tries to get the entity bundle (without version)
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testWikiConfigureWithDotVersion() {
		String entityId = "syn123";
		Long versionNumber = 9L;
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, entityId + "." + versionNumber);
		previewWidget.configure(null, descriptor, null, null);
		
		//verify that it tries to get the entity bundle (with version)
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testWikiConfigureInvalidEntityId() {
		String entityId = "123.9";
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, entityId);
		previewWidget.configure(null, descriptor, null, null);
		
		verify(mockView).addSynapseAlertWidget(any(Widget.class));
		verify(mockSynapseAlert).showError(anyString());
	}
	
	@Test
	public void testWikiConfigureFailure() {
		String exceptionMessage= "my test error message";
		AsyncMockStubber.callFailureWith(new Exception(exceptionMessage)).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, "syn111");
		previewWidget.configure(null, descriptor, null, null);
		
		//verify that it tries to get the entity bundle (without version)
		verify(mockView).addSynapseAlertWidget(any(Widget.class));
		verify(mockSynapseAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testGetPreviewFileContents() {
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("text/plain");
		testFileHandleList.add(fh);
		
		mainFileHandle.setContentType("text/plain");
		mainFileHandle.setFileName("test.txt");
		
		previewWidget.configure(testBundle);
		
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), stringCaptor.capture());
		assertTrue(stringCaptor.getValue().contains("preview=true"));
		verify(mockView).showLoading();
	}
	
	@Test
	public void testGetHtml() {
		mainFileHandle.setContentType("text/html");
		mainFileHandle.setFileName("test.html");
		previewWidget.configure(testBundle);
		
		verify(mockHtmlPreviewWidget).configure(TEST_ENTITY_ID, mainFileHandle);
		verify(mockView).setPreviewWidget(mockHtmlPreviewWidget);
	}
	
	@Test
	public void testGetJupyterNotebook() {
		mainFileHandle.setContentType(APPLICATION_OCTET_STREAM);
		mainFileHandle.setFileName("test.ipynb");
		previewWidget.configure(testBundle);
		
		verify(mockNbConvertPreviewWidget).configure(TEST_ENTITY_ID, mainFileHandle);
		verify(mockView).setPreviewWidget(mockNbConvertPreviewWidget);
	}
}
