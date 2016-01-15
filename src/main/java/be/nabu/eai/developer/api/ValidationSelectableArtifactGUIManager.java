package be.nabu.eai.developer.api;

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;

public interface ValidationSelectableArtifactGUIManager<T extends Artifact> extends ArtifactGUIManager<T> {
	public boolean locate(Validation<?> validation);
}
