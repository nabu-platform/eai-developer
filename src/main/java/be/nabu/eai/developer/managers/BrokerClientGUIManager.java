package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.BrokerClientManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.artifacts.api.ArtifactResolver;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class BrokerClientGUIManager extends BaseGUIManager<DefinedBrokerClient, BrokerClientGUIInstance> {

	public BrokerClientGUIManager() {
		super("Broker Client", DefinedBrokerClient.class, new BrokerClientManager());
	}

	@Override
	protected DefinedBrokerClient display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final DefinedBrokerClient brokerClient = (DefinedBrokerClient) entry.getNode().getArtifact();
		pane.getChildren().add(new Label("TODO: " + brokerClient.getId()));
		return brokerClient;
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected BrokerClientGUIInstance newGUIInstance(ResourceEntry entry) {
		return new BrokerClientGUIInstance(entry);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected DefinedBrokerClient newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		ArtifactResolver resolver = entry.getRepository();
		return new DefinedBrokerClient(entry.getId(), entry.getContainer(), (ArtifactResolver<DefinedKeyStore>) resolver);
	}

	@Override
	protected void setInstance(BrokerClientGUIInstance guiInstance, DefinedBrokerClient instance) {
		guiInstance.setClient(instance);
	}
}
