package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCServiceManager;
import be.nabu.libs.services.jdbc.JDBCService;
import be.nabu.libs.validator.api.ValidationMessage;

public class JDBCServiceGUIInstance implements ArtifactGUIInstance {

	private JDBCService service;
	private ResourceEntry entry;
	
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
	public List<ValidationMessage> save() throws IOException {
		return new JDBCServiceManager().save(entry, service);
	}

	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public boolean isReady() {
		return service != null;
	}

	@Override
	public boolean isEditable() {
		return true;
	}
}