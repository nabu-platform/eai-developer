package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.managers.StructureManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.types.structure.DefinedStructure;
import be.nabu.libs.validator.api.ValidationMessage;

public class StructureGUIInstance implements ArtifactGUIInstance {

	private DefinedStructure structure;
	private Entry entry;
	
	public StructureGUIInstance() {
		// delayed
	}
	
	public StructureGUIInstance(Entry entry, DefinedStructure structure) {
		this.entry = entry;
		this.structure = structure;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return new StructureManager().save((RepositoryEntry) entry, structure);
	}

	@Override
	public boolean isReady() {
		return entry != null && structure != null;
	}
	
	@Override
	public boolean hasChanged() {
		return true;
	}

	public DefinedStructure getStructure() {
		return structure;
	}

	public void setStructure(DefinedStructure structure) {
		this.structure = structure;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean isEditable() {
		return entry.isEditable();
	}
}
