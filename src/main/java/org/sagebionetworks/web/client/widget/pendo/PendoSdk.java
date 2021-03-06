package org.sagebionetworks.web.client.widget.pendo;

import static org.sagebionetworks.web.client.ClientProperties.PENDO_SDK_JS;

import org.sagebionetworks.web.client.resources.ResourceLoader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Wrapper around the Pendo library
 * @author jayhodgson
 *
 */
public class PendoSdk {
	ResourceLoader resourceLoader;
	
	@Inject
	public PendoSdk(
			ResourceLoader resourceLoader
			) {
		this.resourceLoader = resourceLoader;
	}

	private void initJs(final AsyncCallback<Void> callback) {
		if (!resourceLoader.isLoaded(PENDO_SDK_JS)) {
			resourceLoader.requires(PENDO_SDK_JS, callback);
		} else {
			callback.onSuccess(null);
		}
	}

	public void initialize(final String userId, final String email) {
		initJs(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				_initialize(userId, email);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private static native void _initialize(
			String userId,
			String synapseEmail) /*-{
		// Call this whenever information about your visitors becomes available
	    // Please use Strings, Numbers, or Bools for value types.
	    try {
		    	$wnd.pendo.initialize({
		      visitor: {
		        id: userId,   // Required if user is logged in
		        email: synapseEmail// Optional
		        // role:         // Optional
		
		        // You can add any additional visitor level key-values here,
		        // as long as it's not one of the above reserved names.
		      },
		
		      account: {
		        // id:           'ACCOUNT-UNIQUE-ID' // Highly recommended
		        // name:         // Optional
		        // planLevel:    // Optional
		        // planPrice:    // Optional
		        // creationDate: // Optional
		
		        // You can add any additional account level key-values here,
		        // as long as it's not one of the above reserved names.
		      }
		    });
	    } catch(err) {
	    		console.error(err);
	    }
	}-*/;
}