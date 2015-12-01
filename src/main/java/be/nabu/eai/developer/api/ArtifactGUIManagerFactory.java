package be.nabu.eai.developer.api;

import be.nabu.libs.artifacts.api.Artifact;

public interface ArtifactGUIManagerFactory<T extends Artifact> {
	public Class<T> getArtifactClass();
	public ArtifactGUIManager<T> newManager();
}
