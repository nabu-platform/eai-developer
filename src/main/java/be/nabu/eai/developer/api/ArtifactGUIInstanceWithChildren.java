package be.nabu.eai.developer.api;

import java.io.IOException;
import java.util.List;

import be.nabu.libs.validator.api.Validation;

public interface ArtifactGUIInstanceWithChildren extends ArtifactGUIInstance {
	public List<Validation<?>> saveChildren() throws IOException;
}
