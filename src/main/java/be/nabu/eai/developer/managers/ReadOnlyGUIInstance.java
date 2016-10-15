package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.libs.artifacts.ArtifactResolverFactory;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ReadOnlyGUIInstance implements ArtifactGUIInstance {

	private String id;
	private boolean changed;

	public ReadOnlyGUIInstance(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		return Arrays.asList(new ValidationMessage [] { new ValidationMessage(Severity.ERROR, "Can not save this read-only artifact") });
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public Artifact getArtifact() {
		return ArtifactResolverFactory.getInstance().getResolver().resolve(getId());
	}
}
