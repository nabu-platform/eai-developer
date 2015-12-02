package be.nabu.eai.developer.api;

import java.io.IOException;
import java.util.List;

import be.nabu.libs.validator.api.Validation;

public interface ArtifactGUIInstance {
	public String getId();
	public List<Validation<?>> save() throws IOException;
	public boolean hasChanged();
	public void setChanged(boolean changed);
	
	/**
	 * The GUI instances are sometimes returned synchronously but they are actually created asynchronously
	 * This should be set to true once the instance is ready
	 */
	public boolean isReady();
	public boolean isEditable();
	
}
