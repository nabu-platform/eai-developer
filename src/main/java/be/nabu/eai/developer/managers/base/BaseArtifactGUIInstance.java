package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.ValidationMessage;

public class BaseArtifactGUIInstance<T extends Artifact> implements ArtifactGUIInstance {

	private ResourceEntry entry;
	private ArtifactManager<T> artifactManager;
	private T artifact;
	private boolean hasChanged = true, isEditable = true;

	public BaseArtifactGUIInstance(ArtifactManager<T> artifactManager, ResourceEntry entry) {
		this.artifactManager = artifactManager;
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<ValidationMessage> save() throws IOException {
		return artifactManager.save(entry, artifact);
	}

	@Override
	public boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public boolean isReady() {
		return artifact != null;
	}

	public T getArtifact() {
		return artifact;
	}

	public void setArtifact(T artifact) {
		this.artifact = artifact;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	public ResourceEntry getEntry() {
		return entry;
	}

	public void setEntry(ResourceEntry entry) {
		this.entry = entry;
	}
}
