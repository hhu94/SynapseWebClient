package org.sagebionetworks.web.client;

import java.util.function.Consumer;

import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.web.client.utils.FutureUtils;

import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SynapseFutureClient {
	private SynapseClientAsync synapseClient;

	@Inject
	public SynapseFutureClient(SynapseClientAsync synapseClient) {
		this.synapseClient = synapseClient;
	}

	public FluentFuture<SignedTokenInterface> hexDecodeAndDeserialize(String tokenTypeName, String signedTokenString) {
		Consumer<AsyncCallback<SignedTokenInterface>> closure = cb -> synapseClient.hexDecodeAndDeserialize(tokenTypeName, signedTokenString, cb);
		return FutureUtils.getFuture(closure);
	}
}
