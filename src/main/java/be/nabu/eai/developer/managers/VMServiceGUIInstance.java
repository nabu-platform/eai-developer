package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.validator.api.ValidationMessage;

public class VMServiceGUIInstance implements ArtifactGUIInstance {

	private RepositoryEntry entry;
	private VMService service;
	
	public VMServiceGUIInstance() {
		// delayed
	}
	
	public VMServiceGUIInstance(RepositoryEntry entry, VMService service) {
		this.entry = entry;
		this.service = service;
	}

	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return new VMServiceManager().save(entry, service);
	}

	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public boolean isReady() {
		return entry != null && service != null;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public VMService getService() {
		return service;
	}

	public void setService(VMService service) {
		this.service = service;
	}
}