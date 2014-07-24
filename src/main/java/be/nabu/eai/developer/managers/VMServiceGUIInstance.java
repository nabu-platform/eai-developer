package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.validator.api.ValidationMessage;

public class VMServiceGUIInstance implements ArtifactGUIInstance {

	private Entry entry;
	private VMService service;
	
	public VMServiceGUIInstance() {
		// delayed
	}
	
	public VMServiceGUIInstance(Entry entry, VMService service) {
		this.entry = entry;
		this.service = service;
	}

	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return new VMServiceManager().save((RepositoryEntry) entry, service);
	}

	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public boolean isReady() {
		return entry != null && service != null;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	public VMService getService() {
		return service;
	}

	public void setService(VMService service) {
		this.service = service;
	}

	@Override
	public boolean isEditable() {
		return entry.isEditable();
	}
}
