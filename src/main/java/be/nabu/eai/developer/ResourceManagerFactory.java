package be.nabu.eai.developer;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.eai.developer.api.ResourceManager;
import be.nabu.eai.developer.api.ResourceManagerInstance;
import be.nabu.libs.resources.api.Resource;

public class ResourceManagerFactory {
	private static ResourceManagerFactory instance = new ResourceManagerFactory();
	
	private static List<ResourceManager> managers;
	
	public static ResourceManagerFactory getInstance() {
		return instance;
	}
	
	public ResourceManagerInstance manage(Resource resource) {
		for (ResourceManager manager : getManagers()) {
			ResourceManagerInstance instance = manager.manage(resource);
			if (instance != null) {
				return instance;
			}
		}
		return null;
	}

	public static List<ResourceManager> getManagers() {
		if (managers == null) {
			synchronized(ResourceManagerFactory.class) {
				if (managers == null) {
					List<ResourceManager> managers = new ArrayList<ResourceManager>();
					for (ResourceManager resourceManager : ServiceLoader.load(ResourceManager.class)) {
						managers.add(resourceManager);
					}
					ResourceManagerFactory.managers = managers;
				}
			}
		}
		return managers;
	}
}
