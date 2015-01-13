package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.KeyStoreManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class KeyStoreGUIManager extends BaseGUIManager<DefinedKeyStore, KeyStoreGUIInstance> {

	public KeyStoreGUIManager() {
		super("Key Store", DefinedKeyStore.class, new KeyStoreManager());
	}

	@Override
	protected DefinedKeyStore display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final DefinedKeyStore keystore = (DefinedKeyStore) entry.getNode().getArtifact();
		pane.getChildren().add(new Label("TODO: " + keystore.getConfiguration().getAlias()));
		return keystore;
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return Arrays.asList(new SimpleProperty<String>("Password", String.class, true));
	}

	@Override
	protected KeyStoreGUIInstance newGUIInstance(ResourceEntry entry) {
		return new KeyStoreGUIInstance(entry);
	}

	@Override
	protected DefinedKeyStore newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException {
		DefinedKeyStore keystore = new DefinedKeyStore(entry.getId(), entry.getContainer());
		keystore.create(getValue("Password", String.class, values));
		return keystore;
	}

	@Override
	protected void setInstance(KeyStoreGUIInstance guiInstance, DefinedKeyStore instance) {
		guiInstance.setKeystore(instance);
	}
}
