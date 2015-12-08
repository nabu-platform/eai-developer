package be.nabu.eai.developer.managers.base;

import java.io.IOException;

import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;

abstract public class BaseJAXBGUIManager<C, T extends JAXBArtifact<C>> extends BaseConfigurationGUIManager<T, C> {

	public BaseJAXBGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager, Class<C> configurationClass) {
		super(name, artifactClass, artifactManager, configurationClass);
	}

	@Override
	public C getConfiguration(T instance) {
		try {
			return instance.getConfiguration();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Repository getRepository(T instance) {
		return instance.getRepository();
	}

}
