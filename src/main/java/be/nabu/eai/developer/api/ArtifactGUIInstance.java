package be.nabu.eai.developer.api;

import java.io.IOException;
import java.util.List;

import be.nabu.libs.validator.api.ValidationMessage;

public interface ArtifactGUIInstance {
	public String getId();
	public List<ValidationMessage> save() throws IOException;
	public boolean hasChanged();
	
	/**
	 * The GUI instances are sometimes returned synchronously but they are actually created asynchronously
	 * This should be set to true once the instance is ready
	 */
	public boolean isReady();
	public boolean isEditable();
}
