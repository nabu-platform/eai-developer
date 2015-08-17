package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCServiceManager;
import be.nabu.libs.services.jdbc.JDBCService;
import be.nabu.libs.validator.api.Validation;

public class JDBCServiceGUIInstance implements ArtifactGUIInstance {

	private JDBCService service;
	private ResourceEntry entry;
	private boolean changed;
	
	public JDBCServiceGUIInstance() {
		// delayed
	}
	
	public JDBCServiceGUIInstance(ResourceEntry entry) {
		this.entry = entry;
	}

	public JDBCService getService() {
		return service;
	}

	public void setService(JDBCService service) {
		this.service = service;
	}

	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		return new JDBCServiceManager().save(entry, service);
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean isReady() {
		return service != null && entry != null;
	}

	@Override
	public boolean isEditable() {
		return true;
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
