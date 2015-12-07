package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.RefresheableArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.KeyStoreManager;
import be.nabu.libs.validator.api.Validation;

public class KeyStoreGUIInstance implements RefresheableArtifactGUIInstance {

	private ResourceEntry entry;
	private DefinedKeyStore keystore;
	private boolean changed;
	private KeyStoreGUIManager manager;

	public KeyStoreGUIInstance(KeyStoreGUIManager manager, ResourceEntry entry) {
		this.manager = manager;
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		return new KeyStoreManager().save(entry, keystore);
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean isReady() {
		return keystore != null;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	void setKeystore(DefinedKeyStore keystore) {
		this.keystore = keystore;
	}

	public ResourceEntry getEntry() {
		return entry;
	}

	public void setEntry(ResourceEntry entry) {
		this.entry = entry;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void refresh(AnchorPane pane) {
		entry.refresh(true);
		try {
			this.keystore = manager.display(MainController.getInstance(), pane, entry);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not refresh: " + getId(), e);
		}
	}
}
