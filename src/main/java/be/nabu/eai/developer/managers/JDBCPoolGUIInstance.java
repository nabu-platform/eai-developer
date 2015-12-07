package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.RefresheableArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCPoolManager;
import be.nabu.libs.artifacts.jdbc.JDBCPool;
import be.nabu.libs.validator.api.Validation;

public class JDBCPoolGUIInstance implements RefresheableArtifactGUIInstance {

	private JDBCPool pool;
	private ResourceEntry entry;
	private boolean changed;
	private JDBCPoolGUIManager jdbcPoolGUIManager;

	public JDBCPoolGUIInstance(JDBCPoolGUIManager jdbcPoolGUIManager) {
		// delayed
		this.jdbcPoolGUIManager = jdbcPoolGUIManager;
	}
	
	public JDBCPoolGUIInstance(JDBCPoolGUIManager jdbcPoolGUIManager, ResourceEntry entry) {
		this.jdbcPoolGUIManager = jdbcPoolGUIManager;
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		return new JDBCPoolManager().save(entry, pool);
	}

	@Override
	public boolean hasChanged() {
		return changed;
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

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void refresh(AnchorPane pane) {
		try {
			entry.refresh(true);
			pool = jdbcPoolGUIManager.display(MainController.getInstance(), pane, entry);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not refresh: " + getId());
		}
	}
	
}
