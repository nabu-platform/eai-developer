package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.eai.repository.managers.KeyStoreManager;
import be.nabu.libs.validator.api.ValidationMessage;

public class KeyStoreGUIInstance implements ArtifactGUIInstance {

	private ResourceEntry entry;
	private DefinedKeyStore keystore;
	private boolean changed;

	public KeyStoreGUIInstance(ResourceEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
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
}
