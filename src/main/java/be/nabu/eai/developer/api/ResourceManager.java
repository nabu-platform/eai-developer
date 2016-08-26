package be.nabu.eai.developer.api;

import be.nabu.eai.developer.MainController;
import be.nabu.libs.resources.api.Resource;

public interface ResourceManager {
	public ResourceManagerInstance view(MainController controller, Resource resource);
}
