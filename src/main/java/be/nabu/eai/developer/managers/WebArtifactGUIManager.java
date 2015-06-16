package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.repository.artifacts.html.WebArtifact;
import be.nabu.eai.repository.artifacts.html.WebArtifactConfiguration;
import be.nabu.eai.repository.managers.WebArtifactManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class WebArtifactGUIManager extends BaseConfigurationGUIManager<WebArtifact, WebArtifactConfiguration> {

	public WebArtifactGUIManager() {
		super("Web Artifact", WebArtifact.class, new WebArtifactManager(), WebArtifactConfiguration.class);
	}

	@Override
	public WebArtifactConfiguration getConfiguration(WebArtifact instance) {
		try {
			return instance.getConfiguration();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected WebArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new WebArtifact(entry.getId(), entry.getContainer());
	}
}
