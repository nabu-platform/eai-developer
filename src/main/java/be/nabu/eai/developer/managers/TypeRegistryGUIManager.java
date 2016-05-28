package be.nabu.eai.developer.managers;

import java.util.List;

import be.nabu.eai.developer.managers.base.BaseArtifactGUIInstance;
import be.nabu.eai.developer.managers.base.BasePortableGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.api.TypeRegistry;

abstract public class TypeRegistryGUIManager<T extends TypeRegistry & Artifact> extends BasePortableGUIManager<T, BaseArtifactGUIInstance<T>> {

	public TypeRegistryGUIManager(ArtifactManager<T> artifactManager, String artifactName) {
		super(artifactName, artifactManager.getArtifactClass(), artifactManager);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected BaseArtifactGUIInstance<T> newGUIInstance(Entry entry) {
		return new BaseArtifactGUIInstance<T>(this, entry);
	}

	@Override
	protected void setEntry(BaseArtifactGUIInstance<T> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<T> guiInstance, T instance) {
		guiInstance.setArtifact(instance);
	}

}
