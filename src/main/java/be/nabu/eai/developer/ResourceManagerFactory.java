/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
