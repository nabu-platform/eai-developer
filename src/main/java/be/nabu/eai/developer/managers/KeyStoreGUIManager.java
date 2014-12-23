package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.KeyStoreManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;

public class KeyStoreGUIManager implements ArtifactGUIManager<DefinedKeyStore> {

	@Override
	public ArtifactManager<DefinedKeyStore> getArtifactManager() {
		return new KeyStoreManager();
	}

	@Override
	public String getArtifactName() {
		return "Key Store";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("keystore.png");
	}

	@Override
	public Class<DefinedKeyStore> getArtifactClass() {
		return DefinedKeyStore.class;
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Key Store", true);
		final NameOnlyCreateController createController = loader.getController();
		final TextField passwordField = new TextField();
		passwordField.setPromptText("Password");
		createController.getGrdForm().addRow(0, passwordField);
		final KeyStoreGUIInstance instance = new KeyStoreGUIInstance((ResourceEntry) target.itemProperty().get());
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					DefinedKeyStore keystore = new DefinedKeyStore(entry.getId(), entry.getContainer());
					keystore.create(passwordField.getText().isEmpty() ? null : passwordField.getText());
					getArtifactManager().save(entry, keystore);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					instance.setKeystore(keystore);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}

		});
		return instance;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		KeyStoreGUIInstance instance = new KeyStoreGUIInstance((ResourceEntry) target.itemProperty().get());
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		instance.setKeystore(display(controller, pane, target.itemProperty().get()));
		return instance;
	}

	private DefinedKeyStore display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final DefinedKeyStore keystore = (DefinedKeyStore) entry.getNode().getArtifact();
		pane.getChildren().add(new Label("TODO: " + keystore.getConfiguration().getAlias()));
		return keystore;
	}
}
