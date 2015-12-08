package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.artifacts.proxy.DefinedProxy;
import be.nabu.eai.repository.artifacts.proxy.ProxyConfiguration;
import be.nabu.eai.repository.managers.ProxyManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class ProxyGUIManager extends BaseJAXBGUIManager<ProxyConfiguration, DefinedProxy> {

	public ProxyGUIManager() {
		super("Proxy", DefinedProxy.class, new ProxyManager(), ProxyConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected DefinedProxy newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new DefinedProxy(entry.getName(), entry, entry.getRepository());
	}

}
