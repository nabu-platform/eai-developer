package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.eai.repository.managers.BrokerClientManager;
import be.nabu.libs.validator.api.ValidationMessage;

public class BrokerClientGUIInstance implements ArtifactGUIInstance {

	private ResourceEntry entry;
	private DefinedBrokerClient client;

	public BrokerClientGUIInstance(ResourceEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return new BrokerClientManager().save(entry, client);
	}

	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public boolean isReady() {
		return client != null;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	public DefinedBrokerClient getClient() {
		return client;
	}

	void setClient(DefinedBrokerClient client) {
		this.client = client;
	}
}
