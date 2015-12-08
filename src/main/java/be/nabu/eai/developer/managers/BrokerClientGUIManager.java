package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.artifacts.broker.BrokerConfiguration;
import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.eai.repository.managers.BrokerClientManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class BrokerClientGUIManager extends BaseJAXBGUIManager<BrokerConfiguration, DefinedBrokerClient> {

	public BrokerClientGUIManager() {
		super("Broker Client", DefinedBrokerClient.class, new BrokerClientManager(), BrokerConfiguration.class);
	}

	@Override
	protected DefinedBrokerClient newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new DefinedBrokerClient(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

}
