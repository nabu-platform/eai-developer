package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.repository.artifacts.http.DefinedHTTPServer;
import be.nabu.eai.repository.artifacts.http.DefinedHTTPServerConfiguration;
import be.nabu.eai.repository.managers.DefinedHTTPServerManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class DefinedHTTPServerGUIManager extends BaseConfigurationGUIManager<DefinedHTTPServer, DefinedHTTPServerConfiguration> {

	public DefinedHTTPServerGUIManager() {
		super("HTTP Server", DefinedHTTPServer.class, new DefinedHTTPServerManager(), DefinedHTTPServerConfiguration.class);
	}

	@Override
	public DefinedHTTPServerConfiguration getConfiguration(DefinedHTTPServer instance) {
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
	protected DefinedHTTPServer newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new DefinedHTTPServer(entry.getId(), entry.getContainer(), entry.getRepository());
	}
}
