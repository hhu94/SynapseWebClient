package org.sagebionetworks.web.unitclient.presenter;import java.net.URLDecoder;import java.net.URLEncoder;import java.util.Date;import org.sagebionetworks.web.client.GWTWrapper;import org.sagebionetworks.web.client.utils.Callback;import com.google.gwt.i18n.client.DateTimeFormat;import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;import com.google.gwt.user.client.rpc.HasRpcToken;import com.google.gwt.user.client.rpc.ServiceDefTarget;import com.google.gwt.i18n.client.NumberFormat;import com.google.gwt.xhr.client.XMLHttpRequest;public class GWTStub implements GWTWrapper {	public GWTStub() {	}	@Override	public String getHostPageBaseURL() {		return "http://hostpage/url";	}	@Override	public String getModuleBaseURL() {		return "http://baseurl/";	}	@Override	public void assignThisWindowWith(String url) {	}	@Override	public String encodeQueryString(String queryString) {		return URLEncoder.encode(queryString);	}	@Override	public String decodeQueryString(String queryString) {		return URLDecoder.decode(queryString);	}	@Override	public XMLHttpRequest createXMLHttpRequest() {		return null;	}	@Override	public NumberFormat getNumberFormat(String pattern) {		return null;	}	@Override	public String getHostPrefix() {		return null;	}	@Override	public String getCurrentURL() {		return null;	}	@Override	public DateTimeFormat getDateTimeFormat(PredefinedFormat format) {		return null;	}		@Override	public void scheduleExecution(Callback callback, int delay) {	}	@Override	public String getUserAgent() {		return null;	}	@Override	public String getAppVersion() {		return null;	}	@Override	public int nextRandomInt() {		return 0;	}	@Override	public void scheduleDeferred(Callback callback) {	}	@Override	public void addDaysToDate(Date date, int days) {	}	@Override	public boolean isWhitespace(String text) {		return false;	}		@Override	public void newItem(String historyToken, boolean issueEvent) {	}		@Override	public void replaceItem(String historyToken, boolean issueEvent) {	}	@Override	public HasRpcToken asHasRpcToken(Object service) {		return null;	}	@Override	public ServiceDefTarget asServiceDefTarget(Object service) {		return null;	}	@Override	public String getUniqueElementId() {		// TODO Auto-generated method stub		return null;	}	@Override	public void scheduleFixedDelay(Callback callback, int delayMs) {	}	@Override	public void restoreWindowPosition() {	}	@Override	public void saveWindowPosition() {	}}