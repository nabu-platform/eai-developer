package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.BrokerClientManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;

public class BrokerClientGUIManager implements ArtifactGUIManager<DefinedBrokerClient> {

	@Override
	public ArtifactManager<DefinedBrokerClient> getArtifactManager() {
		return new BrokerClientManager();
	}

	@Override
	public String getArtifactName() {
		return "Broker Client";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("brokerclient.png");
	}

	@Override
	public Class<DefinedBrokerClient> getArtifactClass() {
		return DefinedBrokerClient.class;
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(
			new SimpleProperty<String>("Name", String.class)
		)));
		
		final BrokerClientGUIInstance instance = new BrokerClientGUIInstance((ResourceEntry) target.itemProperty().get());
		JDBCServiceGUIManager.buildPopup(controller, updater, "Broker Client", new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					String name = updater.getValue("Name");
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					DefinedBrokerClient brokerClient = new DefinedBrokerClient(entry.getId(), entry.getContainer(), controller.getRepository().getServiceContext().getResolver(DefinedKeyStore.class));
					getArtifactManager().save(entry, brokerClient);
					controller.getRepositoryBrowser().refresh();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					instance.setClient(brokerClient);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}

		});
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		BrokerClientGUIInstance instance = new BrokerClientGUIInstance((ResourceEntry) target.itemProperty().get());
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		instance.setClient(display(controller, pane, target.itemProperty().get()));
		return instance;
	}

	private DefinedBrokerClient display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final DefinedBrokerClient brokerClient = (DefinedBrokerClient) entry.getNode().getArtifact();
		pane.getChildren().add(new Label("TODO: " + brokerClient.getId()));
		return brokerClient;
	}
}
