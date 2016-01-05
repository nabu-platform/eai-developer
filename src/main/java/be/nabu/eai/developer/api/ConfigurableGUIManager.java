package be.nabu.eai.developer.api;

import java.util.Map;

import be.nabu.libs.artifacts.api.Artifact;

public interface ConfigurableGUIManager<T extends Artifact> extends ArtifactGUIManager<T> {
	public void setConfiguration(Map<String, String> configuration);
}
