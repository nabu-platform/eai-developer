package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCPoolManager;
import be.nabu.libs.artifacts.jdbc.JDBCPool;
import be.nabu.libs.validator.api.ValidationMessage;

public class JDBCPoolGUIInstance implements ArtifactGUIInstance {

	private JDBCPool pool;
	private ResourceEntry entry;

	public JDBCPoolGUIInstance() {
		// delayed
	}
	
	public JDBCPoolGUIInstance(ResourceEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return new JDBCPoolManager().save(entry, pool);
	}

	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public boolean isReady() {
		return pool != null && entry != null;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	void setPool(JDBCPool pool) {
		this.pool = pool;
	}

	public ResourceEntry getEntry() {
		return entry;
	}

	public void setEntry(ResourceEntry entry) {
		this.entry = entry;
	}
}
