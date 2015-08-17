package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class BaseArtifactGUIInstance<T extends Artifact> implements ArtifactGUIInstance {

	private Entry entry;
	private ArtifactManager<T> artifactManager;
	private T artifact;
	private boolean hasChanged, isEditable = true;

	public BaseArtifactGUIInstance(ArtifactManager<T> artifactManager, Entry entry) {
		this.artifactManager = artifactManager;
		this.entry = entry;
	}
	
	@Override
	public String getId() {
		return entry.getId();
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		if (entry instanceof ResourceEntry) {
			return artifactManager.save((ResourceEntry) entry, artifact);
		}
		else {
			return Arrays.asList(new ValidationMessage [] { new ValidationMessage(Severity.WARNING, "This item is read-only") });
		}
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

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	@Override
	public void setChanged(boolean changed) {
		this.hasChanged = changed;
	}
}
