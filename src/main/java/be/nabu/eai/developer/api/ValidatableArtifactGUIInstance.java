package be.nabu.eai.developer.api;

import java.util.List;

import be.nabu.libs.validator.api.Validation;

public interface ValidatableArtifactGUIInstance extends ArtifactGUIInstance {
	public List<? extends Validation<?>> validate();
	public boolean locate(Validation<?> validation);
}
