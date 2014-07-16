package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.managers.StructureManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.types.structure.DefinedStructure;
import be.nabu.libs.validator.api.ValidationMessage;

public class StructureGUIInstance implements ArtifactGUIInstance {

	private DefinedStructure structure;
	private RepositoryEntry entry;
	
	public StructureGUIInstance() {
		// delayed
	}
	
	public StructureGUIInstance(RepositoryEntry entry, DefinedStructure structure) {
		this.entry = entry;
		this.structure = structure;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		new StructureManager().save(entry, structure);
		return new ArrayList<ValidationMessage>();
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

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
}
